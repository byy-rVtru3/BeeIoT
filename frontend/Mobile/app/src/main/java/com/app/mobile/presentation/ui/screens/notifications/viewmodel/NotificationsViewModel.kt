package com.app.mobile.presentation.ui.screens.notifications.viewmodel

import com.app.mobile.domain.usecase.notifications.ClearNotificationHistoryUseCase
import com.app.mobile.domain.usecase.notifications.GetNotificationHistoryUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel

class NotificationsViewModel(
    private val getNotificationHistoryUseCase: GetNotificationHistoryUseCase,
    private val clearNotificationHistoryUseCase: ClearNotificationHistoryUseCase
) : BaseViewModel<NotificationsUiState, NotificationsEvent>(NotificationsUiState.Empty) {

    init {
        observeHistory()
    }

    override fun handleError(exception: Throwable) {
        // no-op: history observation does not produce user-visible errors
    }

    private fun observeHistory() {
        launch {
            getNotificationHistoryUseCase().collect { list ->
                updateState {
                    if (list.isEmpty()) NotificationsUiState.Empty
                    else NotificationsUiState.Content(list)
                }
            }
        }
    }

    fun onClear() {
        launch { clearNotificationHistoryUseCase() }
    }

    fun onBackClick() {
        sendEvent(NotificationsEvent.NavigateBack)
    }
}
