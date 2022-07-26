package com.example.basicfirebase.search

import android.os.Parcelable

data class Feature (
    val bbox: List<Double>,
    val center: List<Double>,
    val context: List<Context>,
    val geometry: Geometry,
    val id: String,
    val place_name: String,
    val place_type: List<String>,
    val properties: Properties,
    val relevance: Double,
    val text: String,
    val type: String
)