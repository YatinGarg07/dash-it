package com.example.basicfirebase

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.basicfirebase.databinding.ActivityHomeScreenBinding
import com.example.basicfirebase.search.Feature
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
//import com.mapbox.search.*
//import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList


const val EXTRA_SEARCH_SUGGESTION = "ORIGIN_MARKER_SUGGESTION_FOR_MAP"

class HomeScreen : AppCompatActivity() {
    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var viewModel: HomeScreenViewModel

    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var autoCompleteTextObserver: Observer<String>
    private var pickUpCoordinates : List<Double>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initializing
        viewModel = ViewModelProvider(this)[HomeScreenViewModel::class.java]
        //searchEngine = MapboxSearchSdk.getSearchEngine()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home_screen)
        binding.mainViewModel = viewModel
        binding.lifecycleOwner = this

        viewModel

        adapter = ArrayAdapter(this, R.layout.suggestions_item_list_view, ArrayList())
        viewModel.initialize(adapter)
        //Setting AutoComplete
        autoCompleteTextObserver = Observer<String>{
            if(it=="") pickUpCoordinates = null
//            CoroutineScope(Dispatchers.IO).launch{
                var text : String= ""
                it.iterator().forEach {
                    if(it == ' '){
                        text = "$text%20"
                    }
                    else{
                        text="$text$it"
                    }
                }

                if(it.length>=2) {

                    CoroutineScope(Dispatchers.IO).launch{
                        delay(200)
                        networkCallForSuggestion(text)
                    }

                }


        }
        binding.autocompleteTextView.threshold = 2
        binding.autocompleteTextView.setAdapter(adapter)
        viewModel.whereFromEditText.observe(this,autoCompleteTextObserver)

        binding.autocompleteTextView.setOnItemClickListener { parent, view, position, id ->
                pickUpCoordinates = viewModel.getSuggestions()[position].center
                    goGetRide()
        }

        binding.signOutGoogleBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            GoogleSignIn.getClient(
                this,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            ).signOut()

            finish()

        }


//        binding.linearLayoutGetRide.foreground.alpha= 50

        //one time implementation of getRide button this will get overridden automatically when first time user clicks on the suggestion,
        // then a new implementation of this listener is in goGetRide() function
        binding.linearLayoutGetRide.setOnClickListener {
            Toast.makeText(this, "Please enter the destination", Toast.LENGTH_SHORT).show()
        }

        binding.navFavouriteHome.setOnClickListener {
            val hardCodedHomeLatLong = listOf(76.76808874924124, 30.696438087505946)
            pickUpCoordinates = hardCodedHomeLatLong
            binding.autocompleteTextView.setText( "Sector 47, Chandigarh, India")
            goGetRide()

        }
        binding.navFavouriteWork.setOnClickListener {
            val hardCodedWorkLatLong = listOf(77.20892930637079, 28.689158103991122)
            pickUpCoordinates=hardCodedWorkLatLong
            binding.autocompleteTextView.setText( "University of Delhi, New Delhi, India")
            goGetRide()
        }


    }

    suspend fun networkCallForSuggestion(text: String){
        val job = CoroutineScope(Dispatchers.IO).launch{
            val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$text.json?access_token=${
                getString(R.string.mapbox_access_token)
            }"
            viewModel.fetchSuggestions(url)
        }
        job.cancelAndJoin()
    }

    private fun goGetRide() {
        //Implicit intent to Maps Activity
        binding.linearLayoutGetRide.setOnClickListener {
            if (pickUpCoordinates == null) {
                Toast.makeText(this, "Please enter the destination", Toast.LENGTH_SHORT).show()
            }
            else {

                val intent = Intent(this, MapsActivity::class.java)
                val coordinates = DoubleArray(2)
                coordinates[0]= pickUpCoordinates!![0]
                coordinates[1] = pickUpCoordinates!![1]
                intent.putExtra(EXTRA_SEARCH_SUGGESTION,coordinates)

                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.tween_out);
            }
        }
    }



}