package com.example.dashit

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.dashit.databinding.FragmentSelectDestinationBinding
import com.example.dashit.login.GoogleAuthUIClient
import com.example.dashit.search.Feature
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import kotlinx.coroutines.*


class SelectDestinationFragment : Fragment() {

    companion object{
        val TAG = "SELECT DESTINATION FRAGMENT"
    }

    private lateinit var viewModel: SelectDestinationViewModel
    private lateinit var binding: FragmentSelectDestinationBinding
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var autoCompleteTextObserver: Observer<String>
    private lateinit var navController : NavController
    private var dropOffCoordinates : Point?  = null
    private lateinit var listener : OnCallBackRecieved
    private val googleAuthUiClient by lazy {
        GoogleAuthUIClient.getInstance(
            context = requireActivity().applicationContext,
            oneTapClient = Identity.getSignInClient(requireActivity().applicationContext)
        )
    }
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
            Log.d(TAG, it.toString())
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
//        auth= FirebaseAuth.getInstance()
        binding.model=viewModel
        binding.lifecycleOwner=this

        //setting autocomplete of maps activity
        suggestionList = ArrayList()
        adapter = ArrayAdapter(this.requireContext(), R.layout.suggestions_item_list_view, suggestionList!!
        )
        viewModel.initialize(adapter)

        binding.autocompleteTextView.threshold = 2
        binding.autocompleteTextView.setAdapter(adapter)

        autoCompleteTextObserver= Observer<String> {

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
                else{
//                    withContext(Dispatchers.Main){
//                        adapter.clear()
//                        adapter.notifyDataSetChanged()
//                    }

            }

        }

        viewModel.whereToEditText.observe(this.viewLifecycleOwner,autoCompleteTextObserver)

        val user = if (googleAuthUiClient.getSignedInUser() == null) "Guest" else googleAuthUiClient.getSignedInUser()!!.displayName

        binding.greetings.text = "Good Morning, $user"
        // Inflate the layout for this fragment

        binding.navFavouriteHome.setOnClickListener {
            val hardCodedHomeLatLong = Point.fromLngLat( 76.76808874924124, 30.696438087505946)
            dropOffCoordinates = hardCodedHomeLatLong

            listener.getDropOffCoordinates(dropOffCoordinates!!)
            listener.autoCompleteHaveFocus(false)

            binding.autocompleteTextView.onEditorAction(EditorInfo.IME_ACTION_DONE)
            binding.autocompleteTextView.setText( "Sector 47, Chandigarh, India")
        }
        binding.navFavouriteWork.setOnClickListener {
            val hardCodedHomeLatLong = Point.fromLngLat(77.20892930637079, 28.689158103991122)
            dropOffCoordinates = hardCodedHomeLatLong

            listener.getDropOffCoordinates(dropOffCoordinates!!)
            listener.autoCompleteHaveFocus(false)

            binding.autocompleteTextView.onEditorAction(EditorInfo.IME_ACTION_DONE)
            binding.autocompleteTextView.setText( "University of Delhi, New Delhi, India")
        }
        return binding.root
    }

    private suspend fun networkCallForSuggestion(text: String){
        val job = CoroutineScope(Dispatchers.IO).launch{
            val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$text.json?access_token=${
                getString(R.string.mapbox_access_token)
            }"
            viewModel.fetchSuggestions(url)
        }
        job.cancelAndJoin()
    }

    private fun displaySuggestions(arr : List<Feature>){
        suggestionList?.clear()
        arr.listIterator().forEach {
            suggestionList?.add(it.place_name)
            Log.d(TAG, it.place_name + "Coordinates : ${it.center[1]},${it.center[0]}")
        }
        Log.d(TAG, "${suggestionList?.size}")
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