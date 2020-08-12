package com.example.firebasetesting

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception

const val REQUEST_CODE_SIGN_IN =0
class MainActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        auth.signOut()
        btnRegister.setOnClickListener {
            registerUser()
        }

        btnLogin.setOnClickListener {
            loginUser()
        }
        btnUpdateProfile.setOnClickListener {
            updateProfile()
        }

        //Google Sign-in
        googleSignInButton.setOnClickListener {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.webclient_id))
                .requestEmail()
                .build()

            val signInClient = GoogleSignIn.getClient(this, options)
            signInClient.signInIntent.also {
                startActivityForResult(it, REQUEST_CODE_SIGN_IN)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            account?.let {
                googleAuthForFirebase(it)
            }
        }
    }

    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)

        CoroutineScope(Dispatchers.IO).launch {

            try {
                auth.signInWithCredential(credentials).await()
                withContext(Dispatchers.Main)
                {
                    Toast.makeText(
                        this@MainActivity,
                        "Google Login Success ${auth.currentUser?.displayName}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main)
                {
                    Toast.makeText(this@MainActivity, e.message.toString(), Toast.LENGTH_LONG)
                        .show()
                }
            }

        }
    }


        private fun registerUser() {
            val email = etEmailRegister.text.toString().trim()
            val password = etPasswordRegister.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {

                    try {
                        auth.createUserWithEmailAndPassword(email, password).await()
                        withContext(Dispatchers.Main) {
                            checkLoggedInState()
                        }

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main)
                        {
                            Toast.makeText(
                                this@MainActivity,
                                e.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

            }

        }

        private fun loginUser() {
            val email = etEmailLogin.text.toString().trim()
            val password = etPasswordLogin.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {

                    try {
                        auth.signInWithEmailAndPassword(email, password).await()
                        withContext(Dispatchers.Main) {
                            checkLoggedInState()
                        }

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main)
                        {
                            Toast.makeText(
                                this@MainActivity,
                                e.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

            }

        }

        //TO update user profile
        private fun updateProfile() {

            auth.currentUser?.let {
                val userName = etUsername.text.toString()
                val photoUri =
                    Uri.parse("android.resource://$packageName/${R.drawable.ic_baseline_supervisor_account_24}")
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .setPhotoUri(photoUri)
                    .build()

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        it.updateProfile(profileUpdates).await()
                        withContext(Dispatchers.Main)
                        {
                            checkLoggedInState()
                            Toast.makeText(
                                this@MainActivity,
                                "Updated Successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main)
                        {
                            Toast.makeText(
                                this@MainActivity,
                                e.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

        }

        //Check loginstate and update loginStatus Text
        private fun checkLoggedInState() {
            val user = auth.currentUser
            if (user == null) {
                tvLoggedIn.text = "You are not logged in"
            } else {
                tvLoggedIn.text = "You are logged in"
                etUsername.setText(user.displayName)
                ivProfilePicture.setImageURI(user.photoUrl)
            }

        }
    }
