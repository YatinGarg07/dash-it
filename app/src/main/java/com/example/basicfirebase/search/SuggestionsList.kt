package com.example.basicfirebase.search

data class SuggestionsList(
    val attribution: String,
    val features: List<Feature>,
    val query: List<String>,
    val type: String
)