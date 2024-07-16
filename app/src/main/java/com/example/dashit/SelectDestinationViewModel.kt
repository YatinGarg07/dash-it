package com.example.dashit

import android.app.Application
import android.content.ContentValues
import android.util.Log
import android.widget.ArrayAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.dashit.search.Feature
import com.example.dashit.search.SuggestionsList
import com.google.gson.Gson

class SelectDestinationViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var upAdapter : ArrayAdapter<String>
    private var suggestionsArray: ArrayList<String> = ArrayList()
    private var oldList : List<Feature> = listOf()
     val suggestionsList : MutableLiveData<List<Feature>> = MutableLiveData()

    fun initialize(adapter: ArrayAdapter<String>){
        upAdapter=adapter
    }
    val whereToEditText : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    //This function will be called from activity to start the search


     suspend fun fetchSuggestions(url : String?){
        if (url!=null){
            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(getApplication())

            // Request a string response from the provided URL.
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url,null,
                { response ->

                    val data = Gson().fromJson<SuggestionsList>(response.toString(), SuggestionsList::class.java)

                            suggestionsList.value = data.features



                },
                {
                    Log.d(ContentValues.TAG, it.toString())

                })

            queue.add(jsonObjectRequest)
        }

    }


    suspend fun getDropOffCoordinates(position:Int): List<Double> {

        return suggestionsList.value?.get(position)?.center!!
    }

}