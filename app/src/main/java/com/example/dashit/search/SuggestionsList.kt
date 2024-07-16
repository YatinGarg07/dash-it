package com.example.dashit.search

data class SuggestionsList(
    val attribution: String,
    val features: List<Feature>,
    val query: List<String>,
    val type: String
)