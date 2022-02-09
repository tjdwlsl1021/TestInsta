package com.example.testinsta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.testinsta.databinding.ActivityMainBinding
import com.example.testinsta.navigation.AlarmFragment
import com.example.testinsta.navigation.DetailViewFragment
import com.example.testinsta.navigation.GridFragment
import com.example.testinsta.navigation.UserFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigationnBar()

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