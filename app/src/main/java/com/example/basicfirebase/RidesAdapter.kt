package com.example.basicfirebase

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.basicfirebase.databinding.RideOptionLayoutBinding

const val PRICE_SURCHARGE = 1.5
class RidesAdapter(list : ArrayList<RideOptionData>,private val context : Context, private val listener : OnRideSelect) : RecyclerView.Adapter<RidesAdapter.RideOption>() {
    private var localList = list
    private lateinit var selectedItemName : String
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideOption {
       val binding = RideOptionLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
         val viewHolder = RideOption(binding)

        //click listener
        binding.rideLinearLayout.setOnClickListener {
            Log.d(TAG,"Item Clicked")
            listener.rideSelectedCallback(localList[viewHolder.bindingAdapterPosition].rideTitle)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RideOption, position: Int) {
//        holder.root.setOnClickListener {
//            listener.rideSelectedCallback(data.rideTitle)
//        }

        holder.bind(localList[position])
    }

    override fun getItemCount(): Int {
        return localList.size
    }

   inner class RideOption(private val binding : RideOptionLayoutBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data: RideOptionData){

        selectedItemName = data.rideTitle
        Glide.with(context).load(data.imageURL).into(binding.rideImage)
            binding.rideName.text = data.rideTitle
            binding.rideTravelTime.text = data.rideDuration
            binding.ridePrice.text =  "₹${data.ridePrice}"
        }
    }

    fun updateAdapter(data: ArrayList<RideOptionData>){
        localList = data
        notifyDataSetChanged()
    }

}

public interface OnRideSelect {
    fun rideSelectedCallback(rideName : String)
}

