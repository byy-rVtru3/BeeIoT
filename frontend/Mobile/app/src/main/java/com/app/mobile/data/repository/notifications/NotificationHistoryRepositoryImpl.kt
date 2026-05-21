package com.app.mobile.data.repository.notifications

import com.app.mobile.data.datastore.NotificationHistoryDataSource
import com.app.mobile.domain.models.notifications.NotificationRecord
import com.app.mobile.domain.repository.notifications.NotificationHistoryRepository
import kotlinx.coroutines.flow.Flow

class NotificationHistoryRepositoryImpl(
    private val dataSource: NotificationHistoryDataSource
) : NotificationHistoryRepository {

    override fun getNotifications(): Flow<List<NotificationRecord>> = dataSource.getNotifications()

    override suspend fun save(notification: NotificationRecord) = dataSource.save(notification)

    override suspend fun clearAll() = dataSource.clearAll()
}
