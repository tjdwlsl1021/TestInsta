package com.example.testinsta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.testinsta.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import splitties.activities.start
import splitties.toast.toast

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.emailLoginButton.setOnClickListener {
            signinAndSignup()
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
                } else {
                    // Login if you have account
                }
            }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            start<MainActivity>()
        }
    }
}