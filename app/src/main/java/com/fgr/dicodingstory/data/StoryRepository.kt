package com.fgr.dicodingstory.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.fgr.dicodingstory.database.StoryDatabase
import com.fgr.dicodingstory.retrofit.ApiInterface
import com.fgr.dicodingstory.retrofit.ListStoryItem
import com.fgr.dicodingstory.retrofit.ListStoryItemWithMap
import com.fgr.dicodingstory.retrofit.LoginResult


class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiInterface,
    private val user: LoginResult,
    private val context: Context
) {
    fun getStory(): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, user, context),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    fun getStoryWithLocation(): LiveData<List<ListStoryItemWithMap>> =
        storyDatabase.storyDao().getAllStoryWithMap()
}