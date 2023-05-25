package com.fgr.dicodingstory.di

import android.content.Context
import com.fgr.dicodingstory.data.StoryRepository
import com.fgr.dicodingstory.database.StoryDatabase
import com.fgr.dicodingstory.retrofit.ApiConfig
import com.fgr.dicodingstory.shared_pref.UserPreference

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val userPreference = UserPreference(context)
        val user = userPreference.getUser()
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.apiService
        return StoryRepository(database, apiService, user, context)
    }
}