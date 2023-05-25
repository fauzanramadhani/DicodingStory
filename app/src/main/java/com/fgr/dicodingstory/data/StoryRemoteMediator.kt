package com.fgr.dicodingstory.data

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.fgr.dicodingstory.database.RemoteKeys
import com.fgr.dicodingstory.database.StoryDatabase
import com.fgr.dicodingstory.retrofit.ApiInterface
import com.fgr.dicodingstory.retrofit.ListStoryItem
import com.fgr.dicodingstory.retrofit.ListStoryItemWithMap
import com.fgr.dicodingstory.retrofit.LoginResult
import com.fgr.dicodingstory.utils.dateToMills

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: ApiInterface,
    private val user: LoginResult,
    private val context: Context,
) : RemoteMediator<Int, ListStoryItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH

        //Jika ingin refresh otomatis setiap 1 jam
//        val cacheTimeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
//        return if (System.currentTimeMillis() - database.lastUpdated() >= cacheTimeout) {
//            InitializeAction.SKIP_INITIAL_REFRESH
//        } else {
//            InitializeAction.LAUNCH_INITIAL_REFRESH
//        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        return try {
            val responseData = apiService.getAllStories(
                "Bearer ${user.token}",
                page,
                state.config.pageSize,
                0
            ).listStory!!
            val myResponseData = mutableListOf<ListStoryItem>()
            val responseDataWithMap = apiService.getAllStories(
                "Bearer ${user.token}",
                page,
                state.config.pageSize,
                1
            ).listStory!!
            val myResponseDataWithMap = mutableListOf<ListStoryItemWithMap>()

            responseData.map { story ->
                myResponseData.add(
                    ListStoryItem(
                        story.id,
                        story.photoUrl,
                        dateToMills(story.createdAt).toString(),
                        story.name,
                        story.description,
                        story.lon,
                        story.lat
                    )
                )
            }

            responseDataWithMap.map { story ->
                myResponseDataWithMap.add(
                    ListStoryItemWithMap(
                        story.id,
                        story.photoUrl,
                        dateToMills(story.createdAt).toString(),
                        story.name,
                        story.description,
                        story.lon,
                        story.lat
                    )
                )
            }

            val endOfPaginationReached = myResponseData.isEmpty()
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().deleteRemoteKeys()
                    database.storyDao().deleteAll()
                    database.storyDao().deleteAllStoryWithMap()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = myResponseData.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                database.remoteKeysDao().insertAll(keys)
                database.storyDao().insertStory(myResponseData)
                database.storyDao().insertStoryWithMap(myResponseDataWithMap)
            }

            MediatorResult.Success(endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }
}