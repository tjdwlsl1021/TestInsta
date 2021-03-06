package com.example.testinsta

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.testinsta.databinding.ActivityMainBinding
import com.example.testinsta.navigation.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.tedpark.tedpermission.rx2.TedRx2Permission
import splitties.activities.start
import splitties.toast.toast

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigationnBar()

        binding.bottomNavigation.selectedItemId = R.id.action_home
        registerPushToken()
    }

    /* 푸시 전송 확인 테스트용
    override fun onStop() {
        super.onStop()

        // 파베 인증 사용자 UID!!
        FcmPush.instance.sendMessage("7U2ufBXjjTaeY7hgDISFo0QBBrq1", "hi","bye")
    }*/

    @SuppressLint("CheckResult")
    private fun checkPermission() {
        TedRx2Permission.with(this)
            .setPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
            )
            .request()
            .subscribe({ tedPermissionResult ->
                if (tedPermissionResult.isGranted) {
                    start<AddPhotoActivity>()
                } else {
                    toast("권한허용시 이용가능")
                }
            }, {
            })
    }

    fun initNavigationnBar() {
        binding.bottomNavigation.run {
            setOnItemSelectedListener { item ->
                setToolbarDefault()

                when (item.itemId) {
                    R.id.action_home -> {
                        val detailViewFragment = DetailViewFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, detailViewFragment).commit()
                    }
                    R.id.action_search -> {
                        val gridFragment = GridFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, gridFragment)
                            .commit()
                    }
                    R.id.action_add_photo -> {
                        checkPermission()
                    }
                    R.id.action_favorite_alarm -> {
                        val alarmFragment = AlarmFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, alarmFragment)
                            .commit()
                    }
                    R.id.action_account -> {
                        val userFragment = UserFragment()
                        var bundle = Bundle()
                        var uid = FirebaseAuth.getInstance().currentUser?.uid
                        bundle.putString("destinationUid", uid)
                        userFragment.arguments = bundle
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_content, userFragment)
                            .commit()
                    }
                }
                true
            }
            selectedItemId = R.id.action_home
        }
    }

    fun setToolbarDefault() {
        binding.toolbarUsername.visibility = View.GONE
        binding.toolbarBtnBack.visibility = View.GONE
        binding.toolbarTitleImage.visibility = View.VISIBLE
    }

    fun registerPushToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            Log.d(TAG, "token: $token")
            val map = mutableMapOf<String, Any>()
            map["pushtoken"] = token

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            uid?.let {
                FirebaseFirestore.getInstance().collection("pushtokens").document(uid).set(map)
            } ?: Log.d(TAG, "uid == null")
        })
    }


}