package com.example.testinsta

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.testinsta.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import splitties.activities.start
import splitties.toast.toast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    var auth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.emailLoginButton.setOnClickListener {
            signinAndSignup()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleSignInButton.setOnClickListener {
            googleLogin()
        }
    }

    fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(
            binding.emailEdittext.text.toString(),
            binding.passwordEdittext.text.toString()
        )
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Creating a user account
                    moveMainPage(task.result?.user)
                } else if (task.exception?.message.isNullOrEmpty()) {
                    // Show the error message
                    toast(task.exception?.message.toString())
                } else {
                    // Login if you have accont
                    signinEmail()
                }
            }
    }

    fun signinEmail() {
        auth?.signInWithEmailAndPassword(
            binding.emailEdittext.text.toString(),
            binding.passwordEdittext.text.toString()
        )
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login
                    moveMainPage(task.result?.user)
                } else {
                    // Show the error message
                    toast(task.exception?.message.toString())
                }
            }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            start<MainActivity>()
        }
    }

    fun googleLogin() {
        val signInIntent = googleSignInClient?.signInIntent
        getContent.launch(signInIntent) // 설정화면으로 이동
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    it.data?.let { data ->
                        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                        result?.let {
                            if (result.isSuccess) {
                                val account = result.signInAccount
                                // Second step
                                firebaseAuthWithGoogle(account)
                            }
                        } // TODO: 2022/02/08 result null인 경우 예외처리
                    } // TODO: 2022/02/08 data null인 경우
                }
            }
        }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login
                    moveMainPage(task.result?.user)
                } else {
                    // Show the error message
                    toast(task.exception?.message.toString())
                }
            }
    }
}