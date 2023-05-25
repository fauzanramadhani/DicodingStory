package com.fgr.dicodingstory.ui.home.list_story

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fgr.dicodingstory.databinding.StoryListCardViewBinding
import com.fgr.dicodingstory.retrofit.ListStoryItem
import com.fgr.dicodingstory.ui.home.detail_story.DetailStoryActivity
import com.fgr.dicodingstory.utils.getTimeAgo

class ListStoryAdapter :
    PagingDataAdapter<ListStoryItem, ListStoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    class ViewHolder(private val binding: StoryListCardViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ListStoryItem) {

            binding.storyListCardViewName.text = data.name
            binding.storyListCardViewDate.text =
                getTimeAgo(binding.root.context, data.createdAt.toLong())
            binding.storyListCardViewDescription.text = data.description
            binding.storyListCardViewImage.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(binding.root.context)
                .load(data.photoUrl)
                .into(binding.storyListCardViewImage)

            binding.itemUserLayout.setOnClickListener {
                val intent = Intent(binding.root.context, DetailStoryActivity::class.java)
                intent.putExtra(DetailStoryActivity.ITEM_ID, data.id)
                binding.root.context.startActivity(intent)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            StoryListCardViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ListStoryItem,
                newItem: ListStoryItem
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}