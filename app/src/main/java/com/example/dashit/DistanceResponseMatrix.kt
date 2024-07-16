package com.example.dashit

data class DistanceResponseMatrix(
    val code: String,
    val destinations: List<Destination>,
    val distances: List<List<Double>>,
    val durations: List<List<Double>>,
    val sources: List<Source>
)