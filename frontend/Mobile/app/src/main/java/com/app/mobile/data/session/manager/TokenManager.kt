package com.app.mobile.data.session.manager

import android.content.Context
import androidx.core.content.edit

class TokenManager(context: Context) {

    private val prefs = context.getSharedPreferences("token_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) = prefs.edit { putString(KEY, token) }

    fun getToken(): String? = prefs.getString(KEY, null)

    fun clearToken() = prefs.edit { remove(KEY) }

    companion object {
        private const val KEY = "jwt_token"
    }
}
