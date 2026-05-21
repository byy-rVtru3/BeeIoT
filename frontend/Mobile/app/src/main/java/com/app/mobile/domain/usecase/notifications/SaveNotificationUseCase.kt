package com.app.mobile.domain.usecase.notifications

import com.app.mobile.domain.models.notifications.NotificationRecord
import com.app.mobile.domain.repository.notifications.NotificationHistoryRepository

class SaveNotificationUseCase(
    private val repository: NotificationHistoryRepository
) {
    suspend operator fun invoke(notification: NotificationRecord) {
        repository.save(notification)
    }
}
