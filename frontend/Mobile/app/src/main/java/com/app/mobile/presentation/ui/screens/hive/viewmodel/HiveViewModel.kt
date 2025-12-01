package com.app.mobile.presentation.ui.screens.hive.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.GetHiveUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class HiveViewModel(
    private val getHiveUseCase: GetHiveUseCase
) : ViewModel() {
    private val _hiveUiState = MutableLiveData<HiveUiState>()
    val hiveUiState: LiveData<HiveUiState> = _hiveUiState

    private val _navigationEvent = MutableLiveData<HiveNavigationEvent?>()
    val navigationEvent: LiveData<HiveNavigationEvent?> = _navigationEvent

    val handler = CoroutineExceptionHandler { _, exception ->
        _hiveUiState.value = HiveUiState.Error(exception.message ?: "Unknown error")
        Log.e("HiveViewModel", "Error loading hive", exception)
    }

    fun loadHive(hiveId: Int) {
        _hiveUiState.value = HiveUiState.Loading
        viewModelScope.launch(handler) {
            _hiveUiState.value = getHiveUseCase(hiveId)
                ?.let { HiveUiState.Content(it.toUiModel()) }
                ?: HiveUiState.Error("Улей не найден")
        }
    }

    fun onTemperatureClick() =
        navigateWithId(HiveNavigationEvent::NavigateToTemperatureByHive)

    fun onNoiseClick() =
        navigateWithId(HiveNavigationEvent::NavigateToNoiseByHive)

    fun onWeightClick() =
        navigateWithId(HiveNavigationEvent::NavigateToWeightByHive)

    fun onNotificationsClick() =
        navigateWithId(HiveNavigationEvent::NavigateToNotificationByHive)

    fun onQueenClick() =
        navigateWithId(HiveNavigationEvent::NavigateToQueenByHive)

    fun onWorksClick() =
        navigateWithId(HiveNavigationEvent::NavigateToWorkByHive)

    fun onHiveListClick() {
        _navigationEvent.value = HiveNavigationEvent.NavigateToHiveList
    }

    fun onHiveEditClick() =
        navigateWithId(HiveNavigationEvent::NavigateToHiveEdit)

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }

    private inline fun navigateWithId(navEvent: (Int) -> HiveNavigationEvent) {
        (_hiveUiState.value as? HiveUiState.Content)?.let { content ->
            _navigationEvent.value = navEvent(content.hive.id)
        }
    }
}