package com.fgr.dicodingstory.database

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fgr.dicodingstory.retrofit.ListStoryItem
import com.fgr.dicodingstory.retrofit.ListStoryItemWithMap

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<ListStoryItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryWithMap(storyWithMap: List<ListStoryItemWithMap>)

    @Query("SELECT * FROM story")
    fun getAllStory(): PagingSource<Int, ListStoryItem>

    @Query("SELECT * FROM  story_with_map")
    fun getAllStoryWithMap(): LiveData<List<ListStoryItemWithMap>>

    @Query("DELETE FROM story")
    suspend fun deleteAll(): Int

    @Query("DELETE FROM story_with_map")
    suspend fun deleteAllStoryWithMap(): Int
}