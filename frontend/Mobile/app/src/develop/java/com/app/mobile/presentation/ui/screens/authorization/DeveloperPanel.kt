package com.app.mobile.presentation.ui.screens.authorization

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.mobile.data.mock.MockDataSourceImpl
import com.app.mobile.presentation.validators.ValidationConfig

/**
 * Панель разработчика для develop-версии
 * Показывает кнопки управления mock и валидацией
 */
@Composable
fun DeveloperPanel(
    mockDataSource: MockDataSourceImpl,
    isMockEnabled: MutableState<Boolean>,
    isValidationEnabled: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        // Кнопка переключения валидации
        Button(
            onClick = {
                val newValue = !isValidationEnabled.value
                if (newValue) {
                    ValidationConfig.enableValidation()
                } else {
                    ValidationConfig.disableValidation()
                }
                isValidationEnabled.value = newValue
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isValidationEnabled.value) Color(0xFF2196F3) else Color.Gray
            )
        ) {
            Text(if (isValidationEnabled.value) "VALID ON" else "VALID OFF", color = Color.White)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Кнопка переключения mock
        Button(
            onClick = {
                val newValue = !isMockEnabled.value
                mockDataSource.setMock(newValue)
                isMockEnabled.value = newValue
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isMockEnabled.value) Color(0xFF4CAF50) else Color.Red
            )
        ) {
            Text(if (isMockEnabled.value) "MOCK ON" else "MOCK OFF", color = Color.White)
        }
    }
}

