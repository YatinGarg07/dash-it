package com.example.dashit

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.dashit.databinding.ActivityLoginBinding
import com.example.dashit.login.GoogleAuthUIClient
import com.example.dashit.login.SignInViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel by viewModels<SignInViewModel>()


    private val googleAuthUiClient by lazy {
        GoogleAuthUIClient.getInstance(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    //Google Sign in registerForActivity, handles what to do when activity starts or if you want to do something depending on activity result
//    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//        if (result.resultCode == Activity.RESULT_OK) {
//
//            val credential = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//            val idToken = getAccount(credential)?.idToken
//
//
//
//            when {
//                idToken != null -> {
//                    // Got an ID token from Google. Use it to authenticate
//                    // with Firebase.
//                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
//                    auth.signInWithCredential(firebaseCredential)
//                        .addOnCompleteListener(this) { task ->
//                            if (task.isSuccessful) {
//                                // Sign in success, update UI with the signed-in user's information
//                                Log.d(ContentValues.TAG, "signInWithCredential:success")
//                                val user = auth.currentUser
//                                Toast.makeText(this, "Login Success", Toast.LENGTH_LONG).show()
//                                updateUI(user!!)
//                            } else {
//                                // If sign in fails, display a message to the user.
//                                Log.d(ContentValues.TAG, "signInWithCredential:failure")
//                                Toast.makeText(this, "Login failed", Toast.LENGTH_LONG).show()
//                                // updateUI(null)
//                            }
//
//                            Log.d(ContentValues.TAG, "Got ID token.")
//                        }
//                }
//                else -> {
//                    // Shouldn't happen.
//                    Log.d(ContentValues.TAG, "No ID token!")
//                }
//            }
//
//        }
//    }
//Google Sign in registerForActivity, handles what to do when activity starts or if you want to do something depending on activity result
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                val signInResult = googleAuthUiClient.signInWithIntent(
                    intent = result.data ?: return@launch
                )
                viewModel.onSignInResult(signInResult)
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
        lifecycleScope.launch {
            val signInIntentSender = googleAuthUiClient.signIn()
            startForResult.launch(
                IntentSenderRequest.Builder(
                    signInIntentSender ?: return@launch
                ).build()
            )

        }


    }

    override fun onStart() {
        googleAuthUiClient.getSignedInUser()?.let {user->
            updateUI(user)
        }
//        if (auth.currentUser!=null) updateUI(auth.currentUser!!)
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        auth = Firebase.auth
//
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(
//                com.firebase.ui.auth.R.string.default_web_client_id)).requestEmail()
//                .build()
//        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.signInGoogleBtn.setOnClickListener {
            signIn()
        }

        binding.signInGuestBtn.setOnClickListener {
            updateUI(null)
        }

        binding.signInGoogleBtn.background.alpha = 255

        binding.signInGuestBtn.background.alpha = 255
        
       lifecycleScope.launch {
           repeatOnLifecycle(Lifecycle.State.STARTED){
               viewModel.state.collect(){ state->
                   if(state.isSignInSuccessful){
                       Toast.makeText(applicationContext, "Sign in Successful", Toast.LENGTH_LONG).show()
//                       Log.d(TAG, "onCreate: ")
                       googleAuthUiClient.getSignedInUser()?.let {user->
                            updateUI(user)
                       }
                       viewModel.resetState()

                   }


               }
           }
       }
        
        
    }



    private fun updateUI(user: FirebaseUser?) {
        val intent = Intent(this,HomeScreen::class.java)
        Log.d("Login Activity", "Going to Home Screen")

        startActivity(intent)
//        Toast.makeText(this,"Logged in as ${user.displayName}",Toast.LENGTH_LONG).show()

    }

    companion object{
        private val TAG = "Login Activity"
    }
}