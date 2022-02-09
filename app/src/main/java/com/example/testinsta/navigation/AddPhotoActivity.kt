package com.example.testinsta.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.testinsta.R
import com.example.testinsta.databinding.ActivityAddPhotoBinding
import com.google.firebase.storage.FirebaseStorage
import splitties.toast.toast
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null

    private lateinit var binding: ActivityAddPhotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        storage = FirebaseStorage.getInstance()

        actionPICK.launch(Intent(Intent.ACTION_PICK).setType("image/*"))

        binding.addphotoBtnUpload.setOnClickListener {
            contentUpload()
        }
    }

    private fun contentUpload() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_ $timestamp _.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)

        photoUri?.let {
            storageRef?.putFile(it)?.addOnSuccessListener {
                toast(getString(R.string.upload_success))
            }
        }

    }

    private val actionPICK =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    photoUri = it.data?.data
                    binding.addphotoImage.setImageURI(photoUri)
                }
                else -> finish()
            }
        }
}