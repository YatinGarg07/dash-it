package com.example.dashit

class RideOptionData(img: String, title: String, duration: String, multiplier: Float, price : Int = 0) {
   val imageURL : String = img
    val rideTitle : String = title
    var rideDuration : String = duration
    val rideMultiplier : Float = multiplier
    var ridePrice : Int = price
}