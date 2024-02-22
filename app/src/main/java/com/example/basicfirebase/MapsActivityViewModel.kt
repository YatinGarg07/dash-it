package com.example.basicfirebase

import android.app.Application
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.search.*
import com.mapbox.search.result.ResultAccuracy
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
//import com.stripe.android.paymentsheet.PaymentSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class MapsActivityViewModel(application: Application) : AndroidViewModel(application){
    // Create a LiveData with a String

    val matrixUrl: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val distance : MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }

    val duration : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val rideName : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val rideBill : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(0)
    }

    //val context = LocalContext.current
    //var customerConfig by remember { mutableStateOf<PaymentSheet.CustomerConfiguration?>(null) }
    //var paymentIntentClientSecret by remember { mutableStateOf<String?>(null) }

    private val _customerConfig = MutableStateFlow<PaymentSheet.CustomerConfiguration?>(null)
    val customerConfig = _customerConfig.asStateFlow()

    private val _paymentIntentClientSecret  = MutableStateFlow("")
    val paymentIntentClientSecret = _paymentIntentClientSecret.asStateFlow()


    fun updateRideDetails(s : String, price: Int){
        rideName.value = s
        rideBill.value = price
    }

    suspend fun fetchDistanceMatrix(url : String?){
        if (url!=null){
            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(getApplication())

            // Request a string response from the provided URL.
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url,null,
                { response ->
                    //val distance = response.getJSONObject("distances").toString()
                    val data = Gson().fromJson<DistanceResponseMatrix>(response.toString(), DistanceResponseMatrix::class.java)

                    val number = data.distances[0][1]/1000.0
                    distance.value = number
                    val timeInSec = (data.durations[0][1]).toInt()
                    val timeInHr = (timeInSec/3600)
                    val timeInMin = (timeInSec%3600)/60

                    if (timeInHr!=0){
                    duration.value = "$timeInHr hours $timeInMin mins Travel Time"
                    }
                    else{
                        duration.value = "$timeInMin mins Travel Time"
                    }
                    Log.d(ContentValues.TAG,"Distance : ${distance.value}")
                    Log.d(ContentValues.TAG,"Duration : ${duration.value}")
                },
                {
                    Log.d(ContentValues.TAG, it.toString())

                })

            queue.add(jsonObjectRequest)
        }

    }

    fun sendData(url : String){
        matrixUrl.value = url
        Log.d(TAG,"Inside Send data")
    }


    fun payNowForm(context: Context, billAmount: Int){
        val amt = billAmount.toString()
        val url = "https://pmplwyznb7.execute-api.ap-south-1.amazonaws.com?bill_amount=$billAmount";
        val queue = Volley.newRequestQueue(getApplication())

//        val jsonObj = JSONObject();
//        jsonObj.put("bill_amount",billAmount)

        // Request a string response from the provided URL.
//        val jj = StringRequest(
//            Request.Method.POST,
//            url,
//
//        )
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url,null,
            { response ->
                //val distance = response.getJSONObject("distances").toString()
                if(response!=null){
                    _customerConfig.value = PaymentSheet.CustomerConfiguration(
                        id = response.getString("customer"),
                        ephemeralKeySecret = response.getString("ephemeralKey")
                    )
                    updatePaymentIntentClientSecret(response.getString("paymentIntent"))

                    PaymentConfiguration.init(context, response.getString("publishableKey"))
                }

            },
            {
                Log.d(ContentValues.TAG, it.toString())

            })
        //PaymentSheet

        queue.add(jsonObjectRequest)
    }


    fun updatePaymentIntentClientSecret(newSecret: String){
        _paymentIntentClientSecret.value = newSecret
    }






}