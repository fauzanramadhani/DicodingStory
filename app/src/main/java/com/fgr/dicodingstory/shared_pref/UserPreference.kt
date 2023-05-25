package com.fgr.dicodingstory.shared_pref

import android.content.Context
import com.fgr.dicodingstory.retrofit.LoginResult

class UserPreference(context: Context) {

    companion object {
        private const val PREFS_NAME = "user_pref_name"
        private const val USER_NAME = "user_name"
        private const val USER_ID = "user_id"
        private const val USER_TOKEN = "user_token"
    }

    private val preference = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUser(user: LoginResult) {
        val editor = preference.edit()
        editor.putString(USER_NAME, user.name)
        editor.putString(USER_ID, user.userId)
        editor.putString(USER_TOKEN, user.token)
        editor.apply()
    }

    fun getUser(): LoginResult {
        return LoginResult(
            preference.getString(USER_NAME, "") ?: "",
            preference.getString(USER_ID, "") ?: "",
            preference.getString(USER_TOKEN, "") ?: ""
        )
    }

    fun logoutUser() {
        val editor = preference.edit()
        editor.remove(USER_NAME)
        editor.remove(USER_ID)
        editor.remove(USER_TOKEN)
        editor.apply()
    }
}