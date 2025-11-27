package com.app.mobile.presentation.ui.screens.aboutapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AboutAppViewModel(

) : ViewModel() {

    private val _aboutAppUiState = MutableLiveData<AboutAppUiState>(AboutAppUiState.Content)
    val aboutAppUiState: LiveData<AboutAppUiState> = _aboutAppUiState
}