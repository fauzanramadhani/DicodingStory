package com.fgr.dicodingstory.ui.home.add_story

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fgr.dicodingstory.retrofit.ApiConfig
import com.fgr.dicodingstory.retrofit.GeneralResponse
import com.fgr.dicodingstory.utils.reduceFileImage
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class AddStoryViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    private val _responseBody = MutableLiveData<GeneralResponse>()
    val responseBody: LiveData<GeneralResponse>
        get() = _responseBody

    fun postStory(token: String, file: File, description: String, myLocation: LatLng?) {
        viewModelScope.launch {
            _isLoading.value = true
            val myFile = reduceFileImage(file)
            val fileRequestBody = RequestBody.create(MediaType.parse("image/*"), myFile)
            try {
                val client = if (myLocation != null) {
                    ApiConfig.apiService.postStory(
                        token,
                        MultipartBody.Part.createFormData("photo", file.name, fileRequestBody),
                        RequestBody.create(MediaType.parse("text/plain"), description),
                        RequestBody.create(
                            MediaType.parse("text/plain"),
                            myLocation.latitude.toString()
                        ),
                        RequestBody.create(
                            MediaType.parse("text/plain"),
                            myLocation.longitude.toString()
                        )
                    )
                } else {
                    ApiConfig.apiService.postStory(
                        token,
                        MultipartBody.Part.createFormData("photo", file.name, fileRequestBody),
                        RequestBody.create(MediaType.parse("text/plain"), description),
                        null,
                        null
                    )
                }

                if (client.isError) {
                    _responseBody.value = GeneralResponse(
                        true,
                        client.message
                    )
                } else {
                    //success response
                    _responseBody.value = client
                }

            } catch (e: Exception) {
                _responseBody.value = GeneralResponse(
                    true,
                    e.message.toString()
                )
            } finally {
                _isLoading.value = false
            }

        }
    }
}