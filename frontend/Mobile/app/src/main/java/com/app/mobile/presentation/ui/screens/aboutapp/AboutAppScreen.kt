package com.app.mobile.presentation.ui.screens.aboutapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppNavigationEvent
import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppUiState
import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppViewModel
import com.app.mobile.ui.theme.Dimens
import com.app.mobile.ui.theme.MobileTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AboutAppScreen(aboutAppViewModel: AboutAppViewModel, onBackClick: () -> Unit) {

    val aboutAppUiState by aboutAppViewModel.aboutAppUiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // делаем что-то на резюме
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(aboutAppViewModel.navigationEvent) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            aboutAppViewModel.navigationEvent.collectLatest { event ->
                when (event) {
                    is AboutAppNavigationEvent.NavigateBack -> onBackClick()
                }
            }
        }
    }

    when (val state = aboutAppUiState) {
        is AboutAppUiState.Content -> AboutAppContent(onBackClick)
        is AboutAppUiState.Error -> ErrorMessage(state.message, {})
        is AboutAppUiState.Loading -> FullScreenProgressIndicator()

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutAppContent(onBackClick: () -> Unit) {
  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("О приложении") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.ScreenContentPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.app_info))
        }
    }
  }
}
@Preview(showBackground = true)
@Composable
fun AboutAppContentPreview() {
    MobileTheme {
        AboutAppContent(onBackClick = {})
    }
}
