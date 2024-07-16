package com.example.dashit.login

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)
