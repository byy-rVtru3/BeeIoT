package com.app.mobile.presentation.ui.screens.aboutapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.mobile.R
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppUiState
import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppViewModel

@Composable
fun AboutAppScreen(aboutAppViewModel: AboutAppViewModel) {

    val aboutAppUiState by aboutAppViewModel.aboutAppUiState
        .observeAsState(AboutAppUiState.Loading)

    when (val state = aboutAppUiState) {

        is AboutAppUiState.Content -> AboutAppContent()

        is AboutAppUiState.Success -> AboutAppContentWithMock(
            isMockEnabled = state.isMockEnabled,
            onMockToggle = { aboutAppViewModel.toggleMockMode(it) }
        )

        is AboutAppUiState.Error -> ErrorMessage(state.message) {}

        is AboutAppUiState.Loading -> FullScreenProgressIndicator()
    }
}

@Composable
private fun AboutAppContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Title("О приложении", modifier = Modifier.padding(bottom = 16.dp))
        Text(stringResource(R.string.app_info))
    }
}

@Composable
private fun AboutAppContentWithMock(
    isMockEnabled: Boolean,
    onMockToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Title("О приложении", modifier = Modifier.padding(bottom = 16.dp))
        Text(stringResource(R.string.app_info))

        // Mock switch card (only for develop flavor)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mock режим",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (isMockEnabled) "Используются mock данные" else "Реальный API",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isMockEnabled,
                    onCheckedChange = onMockToggle
                )
            }
        }

        if (isMockEnabled) {
            Text(
                text = "⚠️ Перезапустите приложение для применения изменений",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
