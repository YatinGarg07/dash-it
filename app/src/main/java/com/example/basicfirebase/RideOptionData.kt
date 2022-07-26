package com.example.basicfirebase

import java.time.Duration

class RideOptionData(img: String, title: String, duration: String, multiplier: Float, price : Int = 0) {
   val imageURL : String = img
    val rideTitle : String = title
    var rideDuration : String = duration
    val rideMultiplier : Float = multiplier
    var ridePrice : Int = price
}