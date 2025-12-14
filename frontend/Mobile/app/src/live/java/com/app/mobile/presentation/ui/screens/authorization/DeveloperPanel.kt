package com.app.mobile.presentation.ui.screens.authorization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier

/**
 * Панель разработчика для live-версии
 * Ничего не показывает - пустая заглушка
 */
@Composable
fun DeveloperPanel(
    isValidationEnabled: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    // Ничего не отображаем в live-версии
}
