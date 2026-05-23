package com.app.mobile.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.app.mobile.domain.models.notifications.NotificationRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val NOTIFICATIONS_KEY = stringPreferencesKey("notifications_json")
private const val MAX_NOTIFICATIONS = 100

class NotificationHistoryDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
    private val json: Json
) : NotificationHistoryDataSource {

    override fun getNotifications(): Flow<List<NotificationRecord>> =
        dataStore.data.map { prefs ->
            val raw = prefs[NOTIFICATIONS_KEY] ?: return@map emptyList()
            runCatching {
                json.decodeFromString<List<NotificationRecord>>(raw)
            }.getOrDefault(emptyList())
        }

    override suspend fun save(notification: NotificationRecord) {
        dataStore.edit { prefs ->
            val existing = prefs[NOTIFICATIONS_KEY]
                ?.let { runCatching { json.decodeFromString<List<NotificationRecord>>(it) }.getOrDefault(emptyList()) }
                ?: emptyList()
            val updated = (listOf(notification) + existing).take(MAX_NOTIFICATIONS)
            prefs[NOTIFICATIONS_KEY] = json.encodeToString(updated)
        }
    }

    override suspend fun clearAll() {
        dataStore.edit { prefs -> prefs.remove(NOTIFICATIONS_KEY) }
    }
}
