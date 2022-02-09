package com.example.testinsta

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.testinsta.databinding.ActivityMainBinding
import com.example.testinsta.navigation.*
import com.tedpark.tedpermission.rx2.TedRx2Permission
import splitties.activities.start
import splitties.toast.toast

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigationnBar()

        binding.bottomNavigation.selectedItemId = R.id.action_home
    }

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
}