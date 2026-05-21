package com.app.mobile.domain.usecase.notifications

import com.app.mobile.domain.repository.notifications.NotificationHistoryRepository

class ClearNotificationHistoryUseCase(
    private val repository: NotificationHistoryRepository
) {
    suspend operator fun invoke() = repository.clearAll()
}
