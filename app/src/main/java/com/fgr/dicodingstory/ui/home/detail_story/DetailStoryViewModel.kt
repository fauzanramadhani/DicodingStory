package com.fgr.dicodingstory.ui.home.detail_story

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fgr.dicodingstory.retrofit.ApiConfig
import com.fgr.dicodingstory.retrofit.DetailResponse
import kotlinx.coroutines.launch

class DetailStoryViewModel(
    private val auth: String,
    private val itemId: String
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private val _detailStories = MutableLiveData<DetailResponse>()
    val detailStories: LiveData<DetailResponse>
        get() = _detailStories

    fun getDetailStories() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val client = ApiConfig.apiService.getStoryById(auth, itemId)
                if (client.isSuccessful) {
                    //client success
                    if (!client.body()!!.error) {
                        //success fetching data
                        Log.e("DetailStoryVM", client.body()!!.error.toString())
                        _detailStories.value = client.body()
                    } else {
                        //failed fetching data
                        Log.e("DetailStoryVM", "1")
                        _detailStories.value = DetailResponse(
                            client.body()!!.error,
                            client.body()!!.message,
                            null
                        )
                    }
                } else {
                    //client error
                    _isLoading.value = true
                    Log.e("DetailStoryVM", "3")
                    _detailStories.value = DetailResponse(
                        true,
                        client.message(),
                        null
                    )
                }
            } catch (e: Exception) {
                //unknown error
                Log.e("DetailStoryVM", "4")
                _detailStories.value = DetailResponse(
                    true,
                    e.message.toString(),
                    null
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    init {
        getDetailStories()
    }
}