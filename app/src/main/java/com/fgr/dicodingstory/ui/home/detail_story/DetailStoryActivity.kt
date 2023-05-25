package com.fgr.dicodingstory.ui.home.detail_story

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.fgr.dicodingstory.R
import com.fgr.dicodingstory.component.CustomProgressDialog
import com.fgr.dicodingstory.databinding.ActivityDetailStoryBinding
import com.fgr.dicodingstory.shared_pref.UserPreference
import com.fgr.dicodingstory.utils.DetailStoryModelFactory
import com.fgr.dicodingstory.utils.dateToMills
import com.fgr.dicodingstory.utils.getTimeAgo

class DetailStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailStoryBinding
    private lateinit var viewModel: DetailStoryViewModel
    private val progressDialog by lazy { CustomProgressDialog(this) }

    companion object {
        const val ITEM_ID = "item_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val itemId = intent.getStringExtra(ITEM_ID).toString()
        val auth = UserPreference(this).getUser().token
        val factory = DetailStoryModelFactory("Bearer $auth", itemId)
        viewModel = ViewModelProvider(this, factory)[DetailStoryViewModel::class.java]

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                progressDialog.start(getString(R.string.please_wait))
            } else {
                progressDialog.stop()
            }
        }

        viewModel.detailStories.observe(this) { response ->
            if (response.error) {
                binding.detailStoryRefresh.visibility = View.VISIBLE
                Toast.makeText(this, getString(R.string.time_out), Toast.LENGTH_LONG).show()
            } else {
                //success
                supportActionBar?.title = response.story?.name ?: getString(R.string.name)
                binding.detailStoryName.text = response.story!!.name
                binding.detailStoryDate.text =
                    getTimeAgo(this, dateToMills(response.story.createdAt))
                binding.detailStoryDescription.text = response.story.description
                Glide.with(this)
                    .load(response.story.photoUrl)
                    .into(binding.detailStoryImage)
                playAnimation()
            }
        }

        binding.detailStoryRefresh.setOnClickListener {
            viewModel.getDetailStories()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun playAnimation() {
        val icUser = ObjectAnimator.ofFloat(binding.icUser, View.ALPHA, 1f).setDuration(500)
        val name = ObjectAnimator.ofFloat(binding.detailStoryName, View.ALPHA, 1f).setDuration(500)
        val date = ObjectAnimator.ofFloat(binding.detailStoryDate, View.ALPHA, 1f).setDuration(500)
        val desc =
            ObjectAnimator.ofFloat(binding.detailStoryDescription, View.ALPHA, 1f).setDuration(500)
        val image =
            ObjectAnimator.ofFloat(binding.detailStoryImage, View.ALPHA, 1f).setDuration(500)

        val together = AnimatorSet().apply {
            playTogether(icUser, name)
        }

        AnimatorSet().apply {
            playSequentially(together, date, desc, image)
            start()
        }
    }
}