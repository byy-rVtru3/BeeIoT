package com.app.mobile.domain.repository.notifications

import com.app.mobile.domain.models.notifications.NotificationRecord
import kotlinx.coroutines.flow.Flow

interface NotificationHistoryRepository {
    fun getNotifications(): Flow<List<NotificationRecord>>
    suspend fun save(notification: NotificationRecord)
    suspend fun clearAll()
}
