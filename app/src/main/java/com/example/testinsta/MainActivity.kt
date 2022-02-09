package com.example.testinsta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import splitties.toast.UnreliableToastApi
import splitties.toast.toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

                toast("111")
//        Toast.makeText(this, "Hi There! This is a Toast.", Toast.LENGTH_SHORT).show()
    }
}