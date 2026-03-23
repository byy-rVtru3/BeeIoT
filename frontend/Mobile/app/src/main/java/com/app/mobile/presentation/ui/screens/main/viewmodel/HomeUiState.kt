package com.app.mobile.presentation.ui.screens.main.viewmodel

import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.domain.models.hives.queen.QueenDomainPreview

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Content(
        val hives: List<HiveDomainPreview>,
        val queens: List<QueenDomainPreview>,
        val hubs: List<HubDomain>,
        val works: List<WorkDomain>,
        val isRefreshing: Boolean = false
    ) : HomeUiState
}
