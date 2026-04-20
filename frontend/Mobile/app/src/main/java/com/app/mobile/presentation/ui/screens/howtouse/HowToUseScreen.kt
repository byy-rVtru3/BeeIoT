package com.app.mobile.presentation.ui.screens.howtouse

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.HowToUseAccordion
import com.app.mobile.presentation.ui.components.HowToUseAccordionSection
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.screens.howtouse.viewmodel.HowToUseEvent
import com.app.mobile.presentation.ui.screens.howtouse.viewmodel.HowToUseUiState
import com.app.mobile.presentation.ui.screens.howtouse.viewmodel.HowToUseViewModel
import com.app.mobile.ui.theme.Dimens
import com.app.mobile.ui.theme.MobileTheme

@Composable
fun HowToUseScreen(howToUseViewModel: HowToUseViewModel, onBackClick: () -> Unit) {

    val howToUseUiState by howToUseViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(howToUseViewModel.event) { event ->
        when (event) {
            is HowToUseEvent.NavigateBack -> onBackClick()

            is HowToUseEvent.ShowSnackBar -> {
                snackbarHostState.showSnackbar(
                    event.message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    when (val state = howToUseUiState) {
        is HowToUseUiState.Content -> HowToUseContent(onBackClick, snackbarHostState)
        is HowToUseUiState.Error -> ErrorMessage(state.message, {})
        is HowToUseUiState.Loading -> FullScreenProgressIndicator()
    }
}

@Composable
private fun HowToUseContent(onBackClick: () -> Unit, snackbarHostState: SnackbarHostState) {
    val sections = listOf(
        HowToUseAccordionSection(
            title = stringResource(R.string.how_to_use_section_title_1),
            body = stringResource(R.string.how_to_use_section_body_1)
        ),
        HowToUseAccordionSection(
            title = stringResource(R.string.how_to_use_section_title_2),
            body = stringResource(R.string.how_to_use_section_body_2),
            showStepNumbers = false
        ),
        HowToUseAccordionSection(
            title = stringResource(R.string.how_to_use_section_title_3),
            body = stringResource(R.string.how_to_use_section_body_3)
        )
        
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.how_to_use_title),
                hasBackground = true,
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { innerPadding ->
        HowToUseAccordion(
            sections = sections,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.ScreenContentPadding),
            initiallyExpandedIndex = 0
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HowToUseContentPreview() {
    MobileTheme {
        HowToUseContent(onBackClick = {}, snackbarHostState = remember { SnackbarHostState() })
    }
}
