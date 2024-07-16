package com.example.basicfirebase

import android.app.Application
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.util.Log
import android.widget.ArrayAdapter
import androidx.lifecycle.*
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.basicfirebase.search.Feature
import com.example.basicfirebase.search.SuggestionsList
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.*

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    private var suggestionsList : List<Feature> = listOf()
    private var suggestionsArray: ArrayList<String> = ArrayList()
    private lateinit var upAdapter : ArrayAdapter<String>

     val whereFromEditText : MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    suspend fun fetchSuggestions(url : String?){
        if (url!=null){
            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(getApplication())

            // Request a string response from the provided URL.
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url,null,
                { response ->

                    val data = Gson().fromJson(response.toString(), SuggestionsList::class.java)
                        suggestionsList = data.features
                        displaySuggestions(data.features)

                    Log.i(TAG,"Suggestions Found")

                },
                {
                    Log.d(ContentValues.TAG, it.toString())

                })

            queue.add(jsonObjectRequest)
        }

    }

    private fun displaySuggestions(arr : List<Feature>){
        suggestionsArray.clear()
        arr.listIterator().forEach {

            suggestionsArray.add(it.place_name)
            Log.d(ContentValues.TAG, it.place_name)
        }

        upAdapter.clear()
        upAdapter.addAll(suggestionsArray)
        upAdapter.notifyDataSetChanged()
    }
    fun getSuggestions() : List<Feature>{
        return suggestionsList
    }




     fun initialize( adapter: ArrayAdapter<String>){

        upAdapter=adapter
    }



}