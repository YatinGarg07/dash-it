package com.example.dashit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.dashit.databinding.ActivitySecondBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth


class SecondActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySecondBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(
                com.firebase.ui.auth.R.string.default_web_client_id)).requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_second)

        binding.userDisplayName.text = auth.currentUser?.displayName
        binding.signoutBtn.setOnClickListener {
            signOut()
            auth.currentUser?.delete()
            finish()

        }

        //Toggle Button Group
        val toggleGroup = binding.toggleButton
        //causes one button to select at a time
        toggleGroup.setSingleSelection(true)
        binding.apply {
            button1.setOnClickListener {
                val intent = Intent(this@SecondActivity, MapsActivity::class.java)
                startActivity(intent)
            }

            containedButton.setOnClickListener {
                val intent = Intent(this@SecondActivity, MapsActivity::class.java)
                startActivity(intent)
            }
                textButton.setOnClickListener {
                val intent = Intent(this@SecondActivity, MapsActivity::class.java)
                startActivity(intent)
            }
            outlineButton.setOnClickListener {
                val intent = Intent(this@SecondActivity, MapsActivity::class.java)
                startActivity(intent)
            }
            switch1.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    val intent = Intent(this@SecondActivity, MapsActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
    private fun signOut() {
        // Firebase sign out
        auth.signOut()
        // Google sign out
        googleSignInClient.signOut()
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_Yes ->
                    if (checked) {
                        val intent = Intent(this@SecondActivity, MapsActivity::class.java)
                        view.toggle()
                        startActivity(intent)
                    }
            }
        }
    }


}