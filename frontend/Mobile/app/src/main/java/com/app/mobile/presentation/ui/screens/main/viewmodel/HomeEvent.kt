package com.app.mobile.presentation.ui.screens.main.viewmodel

sealed interface HomeEvent {
    data class NavigateToHive(val hiveName: String) : HomeEvent
    data class NavigateToQueen(val queenName: String) : HomeEvent
    data class NavigateToHub(val hubId: String) : HomeEvent
    data class NavigateToWork(val workId: String, val hiveId: String) : HomeEvent
}
