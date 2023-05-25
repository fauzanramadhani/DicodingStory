package com.fgr.dicodingstory.ui.home.list_story

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.fgr.dicodingstory.R
import com.fgr.dicodingstory.databinding.ActivityListStoryBinding
import com.fgr.dicodingstory.shared_pref.UserPreference
import com.fgr.dicodingstory.ui.authentication.login.LoginActivity
import com.fgr.dicodingstory.ui.home.add_story.AddStoryActivity
import com.fgr.dicodingstory.ui.home.maps_story.MapsActivity
import com.fgr.dicodingstory.utils.ViewModelFactory

class ListStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListStoryBinding
    private val viewModel: ListStoryViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityListStoryBinding.inflate(layoutInflater)
        val userPreference = UserPreference(this)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupMenu(userPreference)

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        val adapter = ListStoryAdapter()
        binding.recyclerView.adapter = adapter.withLoadStateFooter(footer = LoadingStateAdapter {
            adapter.retry()
        })
        viewModel.listStory.observe(this) {
            if (it != null) {
                adapter.submitData(lifecycle, it)
            }
        }

        val launcherAddStoryActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                adapter.refresh()
                scrollToTop()
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
            scrollToTop()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.listUserRefresh.setOnClickListener {
            adapter.refresh()
            scrollToTop()
        }
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this@ListStoryActivity, AddStoryActivity::class.java)
            launcherAddStoryActivity.launch(intent)
        }
    }

    private fun setupMenu(userPreference: UserPreference) {
        this.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.setting -> {
                        startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                        true
                    }

                    R.id.logout -> {
                        //delete user data in shared preference
                        userPreference.logoutUser()
                        startActivity(Intent(this@ListStoryActivity, LoginActivity::class.java))
                        finish()
                        true
                    }

                    R.id.map -> {
                        startActivity(Intent(this@ListStoryActivity, MapsActivity::class.java))
                        true
                    }

                    else -> false
                }
            }

        }, this, Lifecycle.State.STARTED)
    }

    private fun scrollToTop() {
        binding.recyclerView.post {
            binding.recyclerView.smoothScrollToPosition(0)
        }
    }
}