package com.example.basicfirebase

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.icu.number.Precision
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.findFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.basicfirebase.databinding.ActivityMapsBinding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.AnnotationType
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.*
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.*
import com.mapbox.search.result.SearchSuggestion
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class MapsActivity : AppCompatActivity(), SelectDestinationFragment.OnCallBackRecieved, RideOptionsFragment.OnCallBackRecieved {

    private lateinit var binding: ActivityMapsBinding
    private var mapView: MapView? = null
    private lateinit var cameraPosition: CameraOptions
    private lateinit var markerInBitmap: Bitmap
    private var pickUpPoint: Point? = null
    private var dropOffPoint: Point? = null
    private lateinit var listOfPoint: List<Point>
    private lateinit var pointAnnotationManager: PointAnnotationManager

    private val mapboxMap: MapboxMap by lazy {
        binding.mapView.getMapboxMap()
    }
    private val viewModel by viewModels<MapsActivityViewModel>()
    private val mapboxNavigation by lazy {
        if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(this)
                    .accessToken(getString(R.string.mapbox_access_token))
                    .build()
            )
        }
    }
    private lateinit var routeLineOptions: MapboxRouteLineOptions
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView

    //Handles runtime permission of Location
    private val locationPrompt =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    private var selectedRidePrice = 0

    private fun isLocationPermissionGranted() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            locationPrompt.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //get bitmap version of marker only once
        bitmapFromDrawableRes(
            this@MapsActivity,
            R.drawable.ic_baseline_location_on_24
        )?.let { markerInBitmap = it }

        //initializing components
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps)
        mapView = binding.mapView
        binding.mapsViewModel = viewModel
        binding.lifecycleOwner = this


        //initializing mapbox's map
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)
        pointAnnotationManager = mapView!!.annotations.createAnnotationManager(
            AnnotationType.PointAnnotation,
            null
        ) as PointAnnotationManager

        //fetching data from intent
        if (intent.hasExtra(EXTRA_SEARCH_SUGGESTION)) {

            Log.d(TAG, "Inside Maps activity Search Suggestion Found")

            val coordinates = intent.getDoubleArrayExtra(EXTRA_SEARCH_SUGGESTION)
            pickUpPoint = Point.fromLngLat(coordinates?.get(0) ?: 0.00, coordinates?.get(1) ?: 0.00)

            pickUpPointFound(pickUpPoint!!)

        }

        routeLineOptions = MapboxRouteLineOptions.Builder(this)
            .build()
        routeLineApi = MapboxRouteLineApi(routeLineOptions)
        routeLineView = MapboxRouteLineView(routeLineOptions)

        binding.menuButton.setOnClickListener {
            finish()
        }

        binding.menuButton.background.alpha = 255

        viewModel.rideBill.observe(this, Observer {
            selectedRidePrice = it
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        MapboxNavigationProvider.destroy()
        routeLineApi.cancel()
        routeLineView.cancel()
    }

    private fun pickUpPointFound(point: Point) {
        addMarkerToMap(point, markerInBitmap)
        cameraPosition = CameraOptions.Builder()
            .center(point)
            .zoom(11.0)
            .build()
        // mapView!!.getMapboxMap().setCamera(cameraPosition)
        //little animation to move camera
        mapView!!.getMapboxMap().flyTo(cameraPosition, MapAnimationOptions.mapAnimationOptions {
            duration(7000)
        })
    }

    // function to add marker at point in map
    private fun addMarkerToMap(point: Point, bitmap: Bitmap) {

        // Set options for the resulting symbol layer.
        val pointAnnotationOptions: PointAnnotationOptions =
            PointAnnotationOptions().withPoint(point).withIconImage(bitmap)
        // Add the resulting pointAnnotation to the map.

        //remember to maker this a global single instance
        pointAnnotationManager.create(pointAnnotationOptions)
    }

    private fun removeMarkerFromMap(point: Point) {
        // Set options for the resulting symbol layer.
        val pointAnnotationOptions: PointAnnotationOptions =
            PointAnnotationOptions().withPoint(point)
        var annotationID: Long? = null
        pointAnnotationManager.annotations.forEach {
            if (it.point == point) annotationID = it.id
        }
        pointAnnotationManager.delete(
            pointAnnotationOptions.build(
                annotationID!!,
                pointAnnotationManager
            )
        )

    }


    //Below two functions are to convert resource drawable to Bitmap
    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
// copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    override fun autoCompleteHaveFocus(hasFocus: Boolean) {
        if (hasFocus) {
            val params = binding.fragmentContainerView.layoutParams as LinearLayout.LayoutParams
            params.weight = 4.0f
            binding.fragmentContainerView.layoutParams = params
        } else {
            val params = binding.fragmentContainerView.layoutParams as LinearLayout.LayoutParams
            params.weight = 15.0f
            binding.fragmentContainerView.layoutParams = params
        }
    }

    override fun onStart() {
        super.onStart()
        //Check if runtime permission has been granted or not and initializing Fused CLient Provider
        isLocationPermissionGranted()
    }

    //callback from fragment
    override fun getDropOffCoordinates(dropOffCoordinates: Point) {

        dropOffPoint?.let {
            routeLineView.hidePrimaryRoute(mapboxMap.getStyle()!!)
            removeMarkerFromMap(it)
            dropOffPoint = null
        }

        //this line should always be after aobve
        dropOffPoint = dropOffCoordinates
        dropOffPointFound()
    }

    private fun dropOffPointFound() {
        addMarkerToMap(dropOffPoint!!, markerInBitmap)
        cameraPosition = CameraOptions.Builder()
            .center(dropOffPoint)
            .zoom(11.0)
            .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
            .build()
        mapboxMap.flyTo(cameraPosition, MapAnimationOptions.mapAnimationOptions {
            duration(7000)
        })
        //mapboxMap.cameraForCoordinates(listOf(pickUpPoint!!,dropOffPoint!!),EdgeInsets(100.0,100.0,100.0, 100.0))

        //rendering routeline direction on map between pickup and dropoff points
        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .coordinatesList(listOf(pickUpPoint!!, dropOffPoint!!))
            .alternatives(false)
            .build()

        //routeline callback
        val navigationRouterCallback = object : NavigationRouterCallback {
            override fun onCanceled(
                routeOptions: RouteOptions,
                routerOrigin: RouterOrigin
            ) {
                TODO("Not yet implemented")
            }

            override fun onFailure(
                reasons: List<RouterFailure>,
                routeOptions: RouteOptions
            ) {
                Log.e(TAG, "route request failed with $reasons")

            }

            override fun onRoutesReady(
                routes: List<NavigationRoute>,
                routerOrigin: RouterOrigin
            ) {
                //val gson = GsonBuilder().setPrettyPrinting().create()
                mapboxNavigation.setNavigationRoutes(routes)
                routeLineApi.setNavigationRoutes(routes) { value ->
                    routeLineView.renderRouteDrawData(mapboxMap.getStyle()!!, value)
                    routeLineView.hideOriginAndDestinationPoints(mapboxMap.getStyle()!!)
                    if (routeLineView.getPrimaryRouteVisibility(mapboxMap.getStyle()!!) == Visibility.NONE) {
                        routeLineView.showPrimaryRoute(mapboxMap.getStyle()!!)
                    }
                }
//                    mapboxMap.coordinateBoundsForCamera(cameraPosition)

            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            mapboxNavigation.requestRoutes(routeOptions, navigationRouterCallback)
        }
        listOfPoint = listOf(pickUpPoint!!, dropOffPoint!!)

        //to start fetching distance and duration


        fetchFromMatrixApi()

    }

    override fun backToSelectDestinationFragment(isPopFromBackStack: Boolean) {
        if (isPopFromBackStack) {
            if (dropOffPoint != null) {
                removeMarkerFromMap(dropOffPoint!!)
            }
            routeLineView.hidePrimaryRoute(mapboxMap.getStyle()!!)
            dropOffPoint = null

            cameraPosition = CameraOptions.Builder()
                .center(pickUpPoint)
                .zoom(11.0)
                .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
                .build()
            mapboxMap.flyTo(cameraPosition, MapAnimationOptions.mapAnimationOptions {
                duration(7000)
            })
        }
    }

    override fun StripePaymentForm() {
        val context = applicationContext
        //api call
        lifecycleScope.launch {
            viewModel.payNowForm(context,selectedRidePrice)
        }
        binding.paymentForm.setContent {
            StripeGateway()
        }
    }

    @Composable
    fun StripeGateway() {

        val paymentSheet = rememberPaymentSheet(::onPaymentSheetResult)
        val paymentIntentClientSecret by viewModel.paymentIntentClientSecret.collectAsState()
        val config by viewModel.customerConfig.collectAsState()

        if(paymentIntentClientSecret.isNotEmpty()){
            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret = paymentIntentClientSecret,
                configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "Dash Bird",
                    customer = config,

                )
            )
        }

    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                print("Canceled")
                Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
                viewModel.updatePaymentIntentClientSecret("")
            }

            is PaymentSheetResult.Failed -> {
                print("Error: ${paymentSheetResult.error}")
                Toast.makeText(this, "${paymentSheetResult.error.message}", Toast.LENGTH_SHORT).show()
                viewModel.updatePaymentIntentClientSecret("")
            }

            is PaymentSheetResult.Completed -> {
                // Display for example, an order confirmation screen
                print("Completed")
                Toast.makeText(this, "Payment Completed", Toast.LENGTH_SHORT).show()
                viewModel.updatePaymentIntentClientSecret("")
            }
        }
    }


    private fun fetchFromMatrixApi() {
        val url = "https://api.mapbox.com/directions-matrix/v1/mapbox/driving/" +
                "${pickUpPoint?.longitude()}" +
                ",${pickUpPoint?.latitude()}" +
                ";${dropOffPoint?.longitude()}," +
                "${dropOffPoint?.latitude()}" +
                "?annotations=distance,duration&access_token=" +
                getString(R.string.mapbox_access_token)

        viewModel.sendData(url)

    }
}





