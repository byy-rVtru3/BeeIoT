package com.app.mobile.presentation.ui.screens.works.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.hive.WorkUi
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.CustomTextField
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.screens.works.editor.models.WorksEditorActions
import com.app.mobile.presentation.ui.screens.works.editor.viewmodel.WorksEditorNavigationEvent
import com.app.mobile.presentation.ui.screens.works.editor.viewmodel.WorksEditorUiState
import com.app.mobile.presentation.ui.screens.works.editor.viewmodel.WorksEditorViewModel
import com.app.mobile.ui.theme.Dimens

@Composable
fun WorksEditorScreen(
    worksEditorViewModel: WorksEditorViewModel,
    onBackClick: () -> Unit
) {
    val workEditorUiState by worksEditorViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        worksEditorViewModel.loadWork()
    }

    ObserveAsEvents(worksEditorViewModel.event) { event ->
        when (event) {
            is WorksEditorNavigationEvent.NavigateToWorksList -> onBackClick()
        }
    }

    when (val state = workEditorUiState) {
        is WorksEditorUiState.Loading -> FullScreenProgressIndicator()
        is WorksEditorUiState.Error -> ErrorMessage(state.message, onRetry = {})
        is WorksEditorUiState.Content -> {
            val actions = WorksEditorActions(
                onTitleChange = worksEditorViewModel::onTitleChange,
                onTextChange = worksEditorViewModel::onTextChange,
                onSaveClick = worksEditorViewModel::onSaveClick
            )
            WorksEditorContent(
                work = state.work,
                actions = actions,
                onNavigateBack = onBackClick
            )
        }
    }
}

@Composable
fun WorksEditorContent(
    work: WorkUi,
    actions: WorksEditorActions,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = if (work.id.isEmpty()) stringResource(R.string.add_work_title) else stringResource(R.string.edit_work_title),
                onBackClick = onNavigateBack,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.ScreenContentPadding)
        ) {
            CustomTextField(
                value = work.title,
                onValueChange = actions.onTitleChange,
                placeholder = stringResource(R.string.work_title_placeholder) // "Название работы"
            )

            Spacer(modifier = Modifier.height(Dimens.ItemsSpacingLarge))

            Text(
                text = stringResource(R.string.work_text_label), // "Текст работы:"
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Dimens.ItemsSpacingSmall))

            WorkBodyTextField(
                value = work.text,
                onValueChange = actions.onTextChange,
                placeholder = "Lorem Ipsum"
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = stringResource(R.string.save),
                onClick = actions.onSaveClick,
                modifier = Modifier.padding(bottom = Dimens.ButtonSoloVerticalPadding).padding(horizontal = Dimens.ButtonHorizontalPaddingLarge)
            )
        }
    }
}


@Composable
private fun WorkBodyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.outline

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = Dimens.BorderWidthNormal.toPx()
                val y = size.height - strokeWidth / 2
                drawLine(
                    color = borderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth
                )
            },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = false,
        minLines = 3,
        maxLines = 15,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimens.TextFieldPaddingHorizontal,
                        vertical = Dimens.TextFieldPaddingVertical
                    )
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        }
    )
}