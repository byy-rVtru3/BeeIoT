package com.app.mobile.presentation.ui.screens.queen.add.viewmodel


sealed class QueenAddNavigationEvent {
    data object NavigateBack : QueenAddNavigationEvent()
}