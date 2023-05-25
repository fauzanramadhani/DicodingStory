package com.fgr.dicodingstory.ui.home.list_story

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fgr.dicodingstory.data.StoryRepository
import com.fgr.dicodingstory.retrofit.ListStoryItem
import com.fgr.dicodingstory.retrofit.ListStoryItemWithMap

class ListStoryViewModel(
    storyRepository: StoryRepository,
) : ViewModel() {
    val listStory: LiveData<PagingData<ListStoryItem>> by lazy {
        storyRepository.getStory().cachedIn(viewModelScope)
    }
    val listStoryWithMap: LiveData<List<ListStoryItemWithMap>> =
        storyRepository.getStoryWithLocation()
}