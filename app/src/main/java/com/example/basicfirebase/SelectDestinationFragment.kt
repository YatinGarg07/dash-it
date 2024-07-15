package com.example.basicfirebase

import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.basicfirebase.databinding.FragmentSelectDestinationBinding
import com.example.basicfirebase.search.Feature
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import com.mapbox.geojson.Point.fromLngLat
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.*


class SelectDestinationFragment : Fragment() {
    private lateinit var viewModel: SelectDestinationViewModel
    private lateinit var binding: FragmentSelectDestinationBinding
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var autoCompleteTextObserver: Observer<String>
    private lateinit var navController : NavController
    private var dropOffCoordinates : Point?  = null
    private lateinit var listener : OnCallBackRecieved
    private lateinit var auth: FirebaseAuth
    private var latLong : List<Double>? = null
    private var suggestionList : ArrayList<String>? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController= Navigation.findNavController(binding.root)
        binding.autocompleteTextView.setOnItemClickListener { parent, view, position, id ->

            CoroutineScope(Dispatchers.IO).launch {
            latLong = viewModel.getDropOffCoordinates(position)
                dropOffCoordinates = Point.fromLngLat(latLong!![0], latLong!![1])
                withContext(Dispatchers.Main){
                listener.getDropOffCoordinates(dropOffCoordinates!!)
                }
            }
            Log.d(TAG,"$latLong")

            //navController.navigate(R.id.action_selectDestinationFragment_to_rideOptionsFragment)
            listener.autoCompleteHaveFocus(false)

            binding.autocompleteTextView.onEditorAction(EditorInfo.IME_ACTION_DONE)

        }
        viewModel.suggestionsList.observe(viewLifecycleOwner, Observer {
            displaySuggestions(it)
        })

        binding.rideButton.setOnClickListener {
            if(dropOffCoordinates!=null){
                //listener.getDropOffCoordinates(dropOffSearchSuggestion!!)
            dropOffCoordinates=null
            navController.navigate(R.id.action_selectDestinationFragment_to_rideOptionsFragment)
            }
        }

        val chngeListener = View.OnFocusChangeListener(){v ,hasFocus ->
            if(hasFocus){
                listener.autoCompleteHaveFocus(true)
            }
            else{
                listener.autoCompleteHaveFocus(false)
            }
        }
        binding.autocompleteTextView.onFocusChangeListener = chngeListener

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.autocompleteTextView.setText("")
    }


    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            listener = activity as OnCallBackRecieved
        } catch (e: ClassCastException) {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_select_destination,container,false)
        viewModel = ViewModelProvider(this)[SelectDestinationViewModel::class.java]
        auth= FirebaseAuth.getInstance()
        binding.model=viewModel
        binding.lifecycleOwner=this

        //setting autocomplete of maps activity
        suggestionList = ArrayList()
        adapter = ArrayAdapter(this.requireContext(), R.layout.suggestions_item_list_view, suggestionList!!
        )
        viewModel.initialize(adapter)
        binding.autocompleteTextView.threshold = 2
        binding.autocompleteTextView.setAdapter(adapter)

        var counter = 0
        autoCompleteTextObserver= Observer<String> {
            CoroutineScope(Dispatchers.IO).launch{
                var text : String= ""
                it.iterator().forEach {
                    if(it == ' '){
                        text = "$text%20"
                    }
                    else{
                        text="$text$it"
                    }
                }
                val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$text.json?limit=5&access_token=${getString(R.string.mapbox_access_token)}"
                if(it.length>=2) {viewModel.fetchSuggestions(url);}
                counter++;
            }

        }

        viewModel.whereToEditText.observe(this.viewLifecycleOwner,autoCompleteTextObserver)


        binding.greetings.text = "Good Morning, ${auth.currentUser?.displayName}"
        // Inflate the layout for this fragment

        binding.navFavouriteHome.setOnClickListener {
            val hardCodedHomeLatLong = Point.fromLngLat(76.79123642703713,30.69131767037568)
            dropOffCoordinates = hardCodedHomeLatLong

            listener.getDropOffCoordinates(dropOffCoordinates!!)
            listener.autoCompleteHaveFocus(false)

            binding.autocompleteTextView.onEditorAction(EditorInfo.IME_ACTION_DONE)
            binding.autocompleteTextView.setText( "Ram Darbar, Chandigarh, India")
        }
        binding.navFavouriteWork.setOnClickListener {
            val hardCodedHomeLatLong = Point.fromLngLat(76.77766352605758, 30.758382138325004)
            dropOffCoordinates = hardCodedHomeLatLong

            listener.getDropOffCoordinates(dropOffCoordinates!!)
            listener.autoCompleteHaveFocus(false)

            binding.autocompleteTextView.onEditorAction(EditorInfo.IME_ACTION_DONE)
            binding.autocompleteTextView.setText( "Sector 11, Chandigarh, India")
        }
        return binding.root
    }

    private fun displaySuggestions(arr : List<Feature>){
        suggestionList?.clear()
        arr.listIterator().forEach {

            suggestionList?.add(it.place_name)
            Log.d(ContentValues.TAG, it.place_name + "Coordinates : ${it.center[1]},${it.center[0]}")
            Log.d(ContentValues.TAG, (suggestionList?.get(suggestionList!!.size-1)) + "Coordinates : ${it.center[1]},${it.center[0]}")
        }
        Log.d(ContentValues.TAG, "${suggestionList?.size}")
            adapter.clear()
            adapter.addAll(suggestionList!!)
            adapter.notifyDataSetChanged()

    }


    interface OnCallBackRecieved{
        fun getDropOffCoordinates(dropOffCoordinates : Point)
        fun autoCompleteHaveFocus(hasFocus : Boolean)
//        fun sendDropOffPoint(point: Point)
    }

}