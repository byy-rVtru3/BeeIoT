package com.app.mobile.presentation.ui.screens.howtouse.viewmodel

import android.util.Log
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.info.GetLocalInfoContentUseCase
import com.app.mobile.domain.usecase.info.SyncInfoContentUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel

class HowToUseViewModel(
    private val getLocalInfoContentUseCase: GetLocalInfoContentUseCase,
    private val syncInfoContentUseCase: SyncInfoContentUseCase
) : BaseViewModel<HowToUseUiState, HowToUseEvent>(HowToUseUiState.Loading) {

    init {
        loadContent()
    }

    override fun handleError(exception: Throwable) {
        updateState { HowToUseUiState.Error(exception.message ?: "Unknown error") }
        Log.e("HowToUseViewModel", exception.message.toString())
    }

    fun onBackClick() {
        launch {
            sendEvent(HowToUseEvent.NavigateBack)
        }
    }

    fun resetError() = loadContent(forceSync = true)

    private fun loadContent(forceSync: Boolean = false) {
        updateState { HowToUseUiState.Loading }
        launch {
            val localContent = getLocalInfoContentUseCase()
            val localSections = localContent.howToSections.map { it.toUiModel() }
            updateState { HowToUseUiState.Content(localSections) }

            when (val syncResult = syncInfoContentUseCase(forceSync)) {
                is ApiResult.Success -> {
                    val syncedSections = syncResult.data.howToSections.map { it.toUiModel() }
                    updateState { HowToUseUiState.Content(syncedSections) }
                }

                else -> {
                    if (localSections.isEmpty()) {
                        updateState { HowToUseUiState.Error("Не удалось загрузить инструкцию") }
                    }
                }
            }
        }
    }
}
