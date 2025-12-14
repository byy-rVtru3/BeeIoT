package com.app.mobile.presentation.ui.screens.authorization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.mobile.presentation.validators.ValidationConfig
import com.app.mobile.ui.theme.DeveloperPanelPrimary
import com.app.mobile.ui.theme.ValidationDisabled
import com.app.mobile.ui.theme.ValidationEnabled

/**
 * Панель разработчика для develop-версии
 * Показывает FloatingActionButton с кнопкой управления валидацией
 */
@Composable
fun DeveloperPanel(
    isValidationEnabled: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val isExpanded = remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        if (isExpanded.value) {
            SmallFloatingActionButton(
                onClick = {
                    val newValue = !isValidationEnabled.value
                    if (newValue) {
                        ValidationConfig.enableValidation()
                    } else {
                        ValidationConfig.disableValidation()
                    }
                    isValidationEnabled.value = newValue
                },
                containerColor = if (isValidationEnabled.value) ValidationEnabled else ValidationDisabled,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = if (isValidationEnabled.value) "V+" else "V-",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }

        FloatingActionButton(
            onClick = { isExpanded.value = !isExpanded.value },
            containerColor = DeveloperPanelPrimary
        ) {
            Text(
                text = "DEV",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}
