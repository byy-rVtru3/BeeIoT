package com.app.mobile.presentation.ui.screens.works.detail.viewmodel

sealed interface WorkDetailEvent {
    data class NavigateToEdit(val workId: String, val hiveId: String) : WorkDetailEvent
    data class NavigateBack(val hiveId: String) : WorkDetailEvent
    data class ShowSnackBar(val message: String) : WorkDetailEvent
}
