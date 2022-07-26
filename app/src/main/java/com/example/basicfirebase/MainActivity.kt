package com.example.basicfirebase

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.basicfirebase.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
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
                                Log.d(TAG, "signInWithCredential:success")
                                val user = auth.currentUser
                                Toast.makeText(this, "Login Success", Toast.LENGTH_LONG).show()
                                updateUI(user!!)
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.d(TAG, "signInWithCredential:failure")
                                Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show()
                                // updateUI(null)
                            }

                            Log.d(TAG, "Got ID token.")
                        }
                }
                else -> {
                    // Shouldn't happen.
                    Log.d(TAG, "No ID token!")
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        supportActionBar?.hide()

        //Google sign in client used for creating sign in intent in registerForActivityResult
        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(
                com.firebase.ui.auth.R.string.default_web_client_id)).requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        //Go to sign up activity
        binding.gotoSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.googleLoginBtn.setOnClickListener {
            signIn()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser!=null){
            updateUI(currentUser)
        }

    }

    private fun updateUI(user: FirebaseUser) {
        val intent = Intent(this,SecondActivity::class.java)

        startActivity(intent)
        Toast.makeText(this,"Hi Welcome, ${user.displayName}",Toast.LENGTH_LONG).show()
    }


}





