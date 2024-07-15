package com.example.basicfirebase

import android.app.Activity
import android.content.ContentValues.TAG
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.basicfirebase.databinding.FragmentRideOptionsBinding
import com.mapbox.maps.extension.style.expressions.dsl.generated.number
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RideOptionsFragment : Fragment() , OnRideSelect {
    private lateinit var binding : FragmentRideOptionsBinding
    private lateinit var onBackPressedCallback : OnBackPressedCallback
    private lateinit var listener : OnCallBackRecieved
    private lateinit var navController : NavController
    private lateinit var customAdapter: RidesAdapter
    private lateinit var data : ArrayList<RideOptionData>
    private lateinit var rideNameObserver : Observer<String>
    private lateinit var distance : String
    private lateinit var travelTime : String
    private val viewModel by activityViewModels<MapsActivityViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_ride_options,container,false)
        makeAdapter("")
        binding.rideOptionsRecyclerView.adapter = customAdapter
        binding.rideOptionsRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        rideNameObserver = Observer<String>{
            binding.rideButton.text = "Choose $it"
            binding.rideButton.background.alpha = 255
            binding.rideButton.isClickable=true

        }
        binding.rideButton.background.alpha = 50
        binding.rideButton.isClickable = false

        viewModel.rideName.observe(viewLifecycleOwner,rideNameObserver)
        viewModel.duration.observe(viewLifecycleOwner, Observer {
            updateDataDurationAndAdapter(it)
        })
        viewModel.distance.observe(viewLifecycleOwner, Observer {
            val number = String.format("%.2f",it).toDouble()
            binding.selectRideTitle.text = "Select a Ride - $number km"
            updatePricingOnly(number)
        })

        viewModel.matrixUrl.observe(viewLifecycleOwner,Observer{
            Log.d(TAG,"Inside URl observer : URL is : $it")
            CoroutineScope(Dispatchers.IO).launch{
            viewModel.fetchDistanceMatrix(it)
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController= Navigation.findNavController(binding.root)

        onBackPressedCallback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                // you can execute the logic here
                if(isRemoving){
                    // Toast.makeText(context,"isRemoving",Toast.LENGTH_SHORT).show()
                }else{
                    //Toast.makeText(context,"notRemoving",Toast.LENGTH_SHORT).show()
                    listener.backToSelectDestinationFragment(true)
                    //binding.rideButton.background.alpha = 50
                    isEnabled = false
                    activity?.onBackPressed()
                }
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, onBackPressedCallback)

        binding.backButton.setOnClickListener {
            listener.backToSelectDestinationFragment(true)
            navController.popBackStack()
            //navController.navigate(R.id.action_rideOptionsFragment_to_selectDestinationFragment)
            //binding.rideButton.visibility = GONE
        }

        binding.rideButton.setOnClickListener {
            listener.StripePaymentForm()
        }



    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            listener = activity as OnCallBackRecieved
        } catch (e: ClassCastException) {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //unregister listener here
        //activity?.onBackPressedDispatcher
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
    }

    private fun makeAdapter(duration : String){
        data = ArrayList()
        data.add(RideOptionData("https://links.papareact.com/3pn","Dash X", duration, 1.0F))
        data.add(RideOptionData("https://links.papareact.com/5w8","Dash XL", duration, 1.2F))
        data.add(RideOptionData("https://links.papareact.com/7pf","Dash LUX", duration, 1.75F))

         customAdapter = RidesAdapter(data,this.requireContext(),this)
    }

    private fun updateDataDurationAndAdapter(duration : String){
        data.forEach {
            it.rideDuration = duration
        }
        customAdapter.updateAdapter(data)
    }
    private fun updatePricingOnly(distance : Double){
        data.forEach {
            it.ridePrice = (distance* PRICE_SURCHARGE*it.rideMultiplier*10).toInt()
        }
    }

    interface OnCallBackRecieved{
        fun backToSelectDestinationFragment(isPopFromBackStack : Boolean)
        fun StripePaymentForm()
    }


    override fun rideSelectedCallback(rideName: String, bill: Int) {
        viewModel.updateRideDetails(rideName,bill)
    }

}