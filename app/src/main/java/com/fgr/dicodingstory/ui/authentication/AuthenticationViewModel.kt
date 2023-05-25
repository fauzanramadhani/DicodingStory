package com.fgr.dicodingstory.ui.authentication

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fgr.dicodingstory.retrofit.ApiConfig
import com.fgr.dicodingstory.retrofit.GeneralResponse
import com.fgr.dicodingstory.retrofit.LoginResponse
import com.fgr.dicodingstory.shared_pref.UserPreference
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {

    private val _registerMessage = MutableLiveData<GeneralResponse>()
    val registerMessage: LiveData<GeneralResponse>
        get() = _registerMessage
    private val _loginMessage = MutableLiveData<LoginResponse>()
    val loginMessage: LiveData<LoginResponse>
        get() = _loginMessage
    private val _isRegisterLoading = MutableLiveData<Boolean>()
    val isRegisterLoading: LiveData<Boolean>
        get() = _isRegisterLoading
    private val _isLoginLoading = MutableLiveData<Boolean>()
    val isLoginLoading: LiveData<Boolean>
        get() = _isLoginLoading

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isRegisterLoading.value = true
            try {
                val response = ApiConfig.apiService.registerUsers(name, email, password)
                _registerMessage.value = GeneralResponse(
                    response.isError,
                    response.message
                )
            } catch (e: Exception) {
                _registerMessage.value = GeneralResponse(
                    true,
                    e.message.toString()
                )
            } finally {
                _isRegisterLoading.value = false
            }
        }
    }

    fun login(context: Context, email: String, password: String) {
        viewModelScope.launch {
            _isLoginLoading.value = true
            try {
                val response = ApiConfig.apiService.loginUsers(email, password)
                val loginResponse = LoginResponse(
                    response.loginResult,
                    response.error,
                    response.message
                )
                _loginMessage.value = loginResponse
                //Save user data in shared preference
                val userPreference = UserPreference(context)
                userPreference.saveUser(loginResponse.loginResult!!)

            } catch (e: Exception) {
                _loginMessage.value = LoginResponse(
                    loginResult = null,
                    error = true,
                    message = e.message.toString()
                )
            } finally {
                _isLoginLoading.value = false
            }
        }
    }
}