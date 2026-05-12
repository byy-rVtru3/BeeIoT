package com.app.mobile.presentation.ui.screens.howtouse.viewmodel

import com.app.mobile.presentation.models.info.HowToSectionUi

sealed interface HowToUseUiState {
    data class Content(val sections: List<HowToSectionUi>) : HowToUseUiState
    data object Loading : HowToUseUiState
    data class Error(val message: String) : HowToUseUiState
}
