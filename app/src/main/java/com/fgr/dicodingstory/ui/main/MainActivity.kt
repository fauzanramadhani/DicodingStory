package com.fgr.dicodingstory.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.fgr.dicodingstory.databinding.ActivityMainBinding
import com.fgr.dicodingstory.shared_pref.UserPreference
import com.fgr.dicodingstory.ui.authentication.login.LoginActivity
import com.fgr.dicodingstory.ui.home.list_story.ListStoryActivity

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userPreference = UserPreference(this)
        binding.mainProgressBar.visibility = View.VISIBLE
        if (userPreference.getUser().userId.isEmpty()) {
            binding.mainProgressBar.visibility = View.INVISIBLE
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            binding.mainProgressBar.visibility = View.INVISIBLE
            startActivity(Intent(this, ListStoryActivity::class.java))
            finish()
        }
    }
}