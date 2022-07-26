package com.example.basicfirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.basicfirebase.databinding.ActivityNewSignupLayoutBinding

class SignupActivity : AppCompatActivity() {
    private  lateinit var binding : ActivityNewSignupLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_signup_layout);
        return super.onCreate(savedInstanceState)
    }
}