package com.app.mobile.domain.usecase.notifications

import com.app.mobile.domain.models.notifications.NotificationRecord
import com.app.mobile.domain.repository.notifications.NotificationHistoryRepository
import kotlinx.coroutines.flow.Flow

class GetNotificationHistoryUseCase(
    private val repository: NotificationHistoryRepository
) {
    operator fun invoke(): Flow<List<NotificationRecord>> = repository.getNotifications()
}
