package com.app.mobile.presentation.ui.screens.hive.list.vewmodel

import android.util.Log
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toHivePreview
import com.app.mobile.domain.usecase.hives.hive.ArchiveHiveUseCase
import com.app.mobile.domain.usecase.hives.hive.DeleteHiveUseCase
import com.app.mobile.domain.usecase.hives.hive.GetHivesPreviewUseCase
import com.app.mobile.domain.usecase.hives.hive.UnarchiveHiveUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HivesListViewModel(
    private val getHivesPreviewUseCase: GetHivesPreviewUseCase,
    private val deleteHiveUseCase: DeleteHiveUseCase,
    private val archiveHiveUseCase: ArchiveHiveUseCase,
    private val unarchiveHiveUseCase: UnarchiveHiveUseCase
) : BaseViewModel<HivesListUiState, HivesListEvent>(HivesListUiState.Loading) {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    override fun handleError(exception: Throwable) {
        updateState { HivesListUiState.Error(exception.message ?: "Unknown error") }
        Log.e("HivesListViewModel", exception.message.toString())
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun loadHives() {
        updateState { HivesListUiState.Loading }
        launch {
            loadBothLists()
        }
    }

    fun refresh() {
        val current = currentState as? HivesListUiState.Content ?: return
        updateState { current.copy(isRefreshing = true) }
        launch {
            loadBothLists()
        }
    }

    private suspend fun loadBothLists() {
        coroutineScope {
            val activeDeferred = async { getHivesPreviewUseCase(active = true) }
            val archivedDeferred = async { getHivesPreviewUseCase(active = false) }

            val activeResult = activeDeferred.await()
            val archivedResult = archivedDeferred.await()

            if (activeResult is ApiResult.Success && archivedResult is ApiResult.Success) {
                updateState {
                    HivesListUiState.Content(
                        activeHives = activeResult.data.map { it.toHivePreview() },
                        archivedHives = archivedResult.data.map { it.toHivePreview() }
                    )
                }
            } else {
                val errorResult = if (activeResult !is ApiResult.Success) activeResult else archivedResult
                updateState { HivesListUiState.Error(errorResult.toErrorMessage()) }
            }
        }
    }

    fun onRetry() {
        loadHives()
    }

    fun onHiveClick(hiveName: String) {
        if (currentState is HivesListUiState.Content) {
            updateState { HivesListUiState.Loading }
            sendEvent(HivesListEvent.NavigateToHive(hiveName))
        }
    }

    fun onCreateHiveClick() {
        if (currentState is HivesListUiState.Content) {
            updateState { HivesListUiState.Loading }
            sendEvent(HivesListEvent.NavigateToCreateHive)
        }
    }

    fun onDeleteHive(name: String) {
        val current = currentState as? HivesListUiState.Content ?: return
        updateState { current.copy(activeHives = current.activeHives.filter { it.name != name }) }
        launch {
            when (val result = deleteHiveUseCase(name)) {
                is ApiResult.Success -> Unit
                else -> {
                    sendEvent(HivesListEvent.ShowSnackBar(result.toErrorMessage()))
                    loadHives()
                }
            }
        }
    }

    fun onArchiveHive(name: String) {
        val current = currentState as? HivesListUiState.Content ?: return
        val hive = current.activeHives.find { it.name == name } ?: return
        updateState {
            current.copy(
                activeHives = current.activeHives.filter { it.name != name },
                archivedHives = current.archivedHives + hive
            )
        }
        launch {
            when (val result = archiveHiveUseCase(name)) {
                is ApiResult.Success -> Unit
                else -> {
                    sendEvent(HivesListEvent.ShowSnackBar(result.toErrorMessage()))
                    loadHives()
                }
            }
        }
    }

    fun onUnarchiveHive(name: String) {
        val current = currentState as? HivesListUiState.Content ?: return
        val hive = current.archivedHives.find { it.name == name } ?: return
        updateState {
            current.copy(
                archivedHives = current.archivedHives.filter { it.name != name },
                activeHives = current.activeHives + hive
            )
        }
        launch {
            when (val result = unarchiveHiveUseCase(name)) {
                is ApiResult.Success -> Unit
                else -> {
                    sendEvent(HivesListEvent.ShowSnackBar(result.toErrorMessage()))
                    loadHives()
                }
            }
        }
    }
}
