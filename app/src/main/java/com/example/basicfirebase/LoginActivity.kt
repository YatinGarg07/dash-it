package com.example.basicfirebase

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.basicfirebase.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    //Google Sign in registerForActivity, handles what to do when activity starts or if you want to do something depending on activity result
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {

            val credential = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val idToken = getAccount(credential)?.idToken

            when {
                idToken != null -> {
                    // Got an ID token from Google. Use it to authenticate
                    // with Firebase.
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(ContentValues.TAG, "signInWithCredential:success")
                                val user = auth.currentUser
                                Toast.makeText(this, "Login Success", Toast.LENGTH_LONG).show()
                                updateUI(user!!)
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.d(ContentValues.TAG, "signInWithCredential:failure")
                                Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show()
                                // updateUI(null)
                            }

                            Log.d(ContentValues.TAG, "Got ID token.")
                        }
                }
                else -> {
                    // Shouldn't happen.
                    Log.d(ContentValues.TAG, "No ID token!")
                }
            }

        }
    }
    fun getAccount(task: Task<GoogleSignInAccount?>): GoogleSignInAccount? {
        var account: GoogleSignInAccount? = null
        try {
            account = task.getResult(ApiException::class.java)
        } catch (ex: ApiException) {
            // TODO: handle exception
            println("MESSAGE: " + ex.message)
            ex.printStackTrace()
        }
        return account
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startForResult.launch(signInIntent)

    }

    override fun onStart() {
        if (auth.currentUser!=null) updateUI(auth.currentUser!!)
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(
                com.firebase.ui.auth.R.string.default_web_client_id)).requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signInGoogleBtn.setOnClickListener {
            signIn()
        }

        binding.signInGoogleBtn.background.alpha = 255
    }

    private fun updateUI(user: FirebaseUser) {
        val intent = Intent(this,HomeScreen::class.java)

        startActivity(intent)
        Toast.makeText(this,"Logged in as ${user.displayName}",Toast.LENGTH_LONG).show()

    }
}