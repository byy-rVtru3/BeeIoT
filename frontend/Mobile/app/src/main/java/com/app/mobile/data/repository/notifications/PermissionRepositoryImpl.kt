package com.app.mobile.data.repository.notifications

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.app.mobile.domain.repository.notifications.PermissionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val POST_NOTIFICATIONS_KEY = booleanPreferencesKey("post_notifications")

class PermissionRepositoryImpl(
	private val dataStore: DataStore<Preferences>
) : PermissionRepository {

	override suspend fun hasAskedForPermission(): Boolean {
		return dataStore.data.map { preferences ->
			preferences[POST_NOTIFICATIONS_KEY] ?: false
		}.first()
	}

	override suspend fun savePermissionAsked(hasAsked: Boolean) {
		dataStore.edit { preferences ->
			preferences[POST_NOTIFICATIONS_KEY] = hasAsked
		}
	}
}