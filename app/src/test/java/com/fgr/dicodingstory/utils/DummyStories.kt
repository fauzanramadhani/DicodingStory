package com.fgr.dicodingstory.utils

import com.fgr.dicodingstory.retrofit.ListStoryItem

object DataDummy {
    fun generateDummyStories() : List<ListStoryItem> {
        val newList = ArrayList<ListStoryItem>()
        for (i in 0..10) {
            val stories = ListStoryItem(
                photoUrl = "photo_url ",
                name = "Story $i",
                description = "Description $i",
                lon = 1.0,
                id = "id $i",
                lat = 2.0,
                createdAt = "2022-01-08T06:34:18.598Z"
            )
            newList.add(stories)
        }
        return newList
    }
}