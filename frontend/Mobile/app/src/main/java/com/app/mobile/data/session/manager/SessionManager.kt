package com.app.mobile.data.session.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

class SessionManager(context: Context) {
    private val dataStore: DataStore<Preferences> = context.dataStore
    private val userIdKey = intPreferencesKey("current_user_id")

    val currentUserId: Flow<Int?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }

        }.map { it[userIdKey] }

    suspend fun saveCurrentUser(userId: Int) {
        dataStore.edit { it[userIdKey] = userId }
    }

    suspend fun getCurrentUserId(): Int? = dataStore.data.first()[userIdKey]

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val Context.dataStore by preferencesDataStore("session_manager")
    }
}