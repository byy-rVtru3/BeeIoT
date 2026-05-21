package com.app.mobile.data.datastore

import com.app.mobile.domain.models.notifications.NotificationRecord
import kotlinx.coroutines.flow.Flow

interface NotificationHistoryDataSource {
    fun getNotifications(): Flow<List<NotificationRecord>>
    suspend fun save(notification: NotificationRecord)
    suspend fun clearAll()
}
