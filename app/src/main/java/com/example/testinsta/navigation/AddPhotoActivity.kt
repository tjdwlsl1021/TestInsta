package com.example.testinsta.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.testinsta.R
import com.example.testinsta.databinding.ActivityAddPhotoBinding
import com.example.testinsta.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import splitties.toast.toast
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPhotoBinding

    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null

    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        actionPICK.launch(Intent(Intent.ACTION_PICK).setType("image/*"))

        binding.addphotoBtnUpload.setOnClickListener {
            contentUpload()
        }
    }

    private fun contentUpload() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_ $timestamp _.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)



        // Firestore Database
        photoUri?.let {
            storageRef?.putFile(it)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }?.addOnSuccessListener { uri ->
                val contentDTO = ContentDTO()
                contentDTO.imageUrl = uri.toString()
                contentDTO.uid = auth?.currentUser?.uid
                contentDTO.userId = auth?.currentUser?.email
                contentDTO.explain = binding.addphotoEditExplain.text.toString()
                contentDTO.timestamp = System.currentTimeMillis()

                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)
                finish()
            }

            // Storage 파일업로드
            /*storageRef?.putFile(it)?.addOnSuccessListener {

                toast(getString(R.string.upload_success))

                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val contentDTO = ContentDTO()
                    contentDTO.imageUrl = uri.toString()
                    contentDTO.uid = auth?.currentUser?.uid
                    contentDTO.userId = auth?.currentUser?.email
                    contentDTO.explain = binding.addphotoEditExplain.text.toString()
                    contentDTO.timestamp = System.currentTimeMillis()

                    firestore?.collection("images")?.document()?.set(contentDTO)

                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }*/
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