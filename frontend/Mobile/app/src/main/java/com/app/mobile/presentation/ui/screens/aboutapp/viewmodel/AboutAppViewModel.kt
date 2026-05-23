package com.app.mobile.presentation.ui.screens.aboutapp.viewmodel

import android.util.Log
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.usecase.info.GetLocalInfoContentUseCase
import com.app.mobile.domain.usecase.info.SyncInfoContentUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel

class AboutAppViewModel(
    private val getLocalInfoContentUseCase: GetLocalInfoContentUseCase,
    private val syncInfoContentUseCase: SyncInfoContentUseCase
) : BaseViewModel<AboutAppUiState, AboutAppEvent>(AboutAppUiState.Loading) {

    init {
        loadContent()
    }

    override fun handleError(exception: Throwable) {
        updateState { AboutAppUiState.Error(exception.message ?: "Unknown error") }
        Log.e("AboutAppViewModel", exception.message.toString())
    }

    fun onBackClick() {
        launch {
            sendEvent(AboutAppEvent.NavigateBack)
        }
    }

    fun resetError() = loadContent(forceSync = true)

    private fun loadContent(forceSync: Boolean = false) {
        updateState { AboutAppUiState.Loading }
        launch {
            val localContent = getLocalInfoContentUseCase()
            updateState { AboutAppUiState.Content(localContent.aboutText) }

            when (val syncResult = syncInfoContentUseCase(forceSync)) {
                is ApiResult.Success -> {
                    updateState { AboutAppUiState.Content(syncResult.data.aboutText) }
                }

                else -> {
                    if (localContent.aboutText.isBlank()) {
                        updateState {
                            AboutAppUiState.Error("Не удалось загрузить информацию о приложении")
                        }
                    }
                }
            }
        }
    }
}