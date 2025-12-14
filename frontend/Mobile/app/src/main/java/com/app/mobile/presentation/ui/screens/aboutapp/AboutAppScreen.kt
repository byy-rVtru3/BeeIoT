package com.app.mobile.presentation.ui.screens.aboutapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.app.mobile.R
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppUiState
import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppViewModel
import com.app.mobile.ui.theme.Dimens
import com.app.mobile.ui.theme.MobileTheme

@Composable
fun AboutAppScreen(aboutAppViewModel: AboutAppViewModel) {

    val aboutAppUiState by aboutAppViewModel.aboutAppUiState.observeAsState(AboutAppUiState.Content)

    when (val state = aboutAppUiState) {
        is AboutAppUiState.Content -> AboutAppContent()
        is AboutAppUiState.Error -> ErrorMessage(state.message, {})
        is AboutAppUiState.Loading -> FullScreenProgressIndicator()

    }
}

@Composable
private fun AboutAppContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.ScreenContentPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Title("О приложении", modifier = Modifier.padding(bottom = Dimens.ItemsSpacingMedium))
        Text(stringResource(R.string.app_info))
    }
}

@Preview(showBackground = true)
@Composable
fun AboutAppContentPreview() {
    MobileTheme {
        AboutAppContent()
    }
}
