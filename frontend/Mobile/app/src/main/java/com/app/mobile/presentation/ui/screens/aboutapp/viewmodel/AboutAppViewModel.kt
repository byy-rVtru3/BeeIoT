package com.app.mobile.presentation.ui.screens.aboutapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.mobile.data.mock.MockDataSource

class AboutAppViewModel(
    private val mockDataSource: MockDataSource
) : ViewModel() {

    private val _aboutAppUiState = MutableLiveData<AboutAppUiState>(AboutAppUiState.Content)
    val aboutAppUiState: LiveData<AboutAppUiState> = _aboutAppUiState

    init {
        loadMockState()
    }

    private fun loadMockState() {
        val isMock = mockDataSource.isMock()
        _aboutAppUiState.value = AboutAppUiState.Success(isMock)
    }

    fun toggleMockMode(enabled: Boolean) {
        mockDataSource.setMock(enabled)
        _aboutAppUiState.value = AboutAppUiState.Success(enabled)
    }
}