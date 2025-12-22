package com.app.mobile.presentation.ui.screens.hive.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.hive.HiveEditorModel
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.CustomTextField
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.screens.hive.editor.models.HiveEditorActions
import com.app.mobile.presentation.ui.screens.hive.editor.viewmodel.HiveEditorNavigationEvent
import com.app.mobile.presentation.ui.screens.hive.editor.viewmodel.HiveEditorUiState
import com.app.mobile.presentation.ui.screens.hive.editor.viewmodel.HiveEditorViewModel
import com.app.mobile.ui.theme.Dimens
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HiveEditorScreen(
    hiveEditorViewModel: HiveEditorViewModel,
    onBackClick: () -> Unit,
    onCreateQueenClick: () -> Unit,
    onCreateHubClick: () -> Unit
) {
    val hiveEditorUiState by hiveEditorViewModel.hiveEditorUiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hiveEditorViewModel.loadHive()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(hiveEditorViewModel.navigationEvent) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            hiveEditorViewModel.navigationEvent.collectLatest { event ->
                when (event) {
                    is HiveEditorNavigationEvent.NavigateToCreateQueen -> onCreateQueenClick()
                    is HiveEditorNavigationEvent.NavigateToCreateHub -> onCreateHubClick()
                    is HiveEditorNavigationEvent.NavigateBack -> onBackClick()
                }
            }
        }
    }

    when (val state = hiveEditorUiState) {
        is HiveEditorUiState.Loading -> FullScreenProgressIndicator()
        is HiveEditorUiState.Error -> ErrorMessage(state.message, onRetry = {})
        is HiveEditorUiState.Content -> {
            val actions = HiveEditorActions(
                onNameChange = hiveEditorViewModel::onNameChange,
                onCreateQueenClick = hiveEditorViewModel::onCreateQueenClick,
                onCreateHubClick = hiveEditorViewModel::onCreateHubClick,
                onAddQueenClick = hiveEditorViewModel::onQueenAdd,
                onAddHubClick = hiveEditorViewModel::onHubAdd,
                onSaveClick = hiveEditorViewModel::onSaveClick
            )
            HiveEditorContent(state.hiveEditorModel, actions, onBackClick)
        }
    }
}

@Composable
fun HiveEditorContent(
    hiveEditorModel: HiveEditorModel,
    actions: HiveEditorActions,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.hive_edit_title), // Убедись, что строка есть в ресурсах
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingLarge)
        ) {

            // Поле ввода названия
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)) {
                Text(
                    text = stringResource(R.string.hive_name_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CustomTextField(
                    value = hiveEditorModel.name,
                    onValueChange = actions.onNameChange,
                    placeholder = stringResource(R.string.hive_name_placeholder),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Секция Хабов (Grid)
            SelectionGridSection(
                title = stringResource(R.string.available_hubs),
                items = hiveEditorModel.hubs.map { it.id to it.name },
                selectedId = hiveEditorModel.connectedHubId,
                onItemSelected = actions.onAddHubClick,
                onCreateClick = actions.onCreateHubClick,
                iconVector = Icons.Rounded.Sensors // Иконка для хаба
            )

            // Секция Маток (Grid)
            SelectionGridSection(
                title = stringResource(R.string.available_queens),
                items = hiveEditorModel.queens.map { it.id to it.name },
                selectedId = hiveEditorModel.connectedQueenId,
                onItemSelected = actions.onAddQueenClick,
                onCreateClick = actions.onCreateQueenClick,
                // Тут можно добавить другую логику отображения, если нужно больше полей
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = stringResource(R.string.save),
                onClick = actions.onSaveClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectionGridSection(
    title: String,
    items: List<Pair<String, String>>, // ID, Name
    selectedId: String?,
    onItemSelected: (String) -> Unit,
    onCreateClick: () -> Unit,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // FlowRow автоматически переносит элементы на новую строку
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
        ) {
            // Рендерим существующие элементы
            items.forEach { (id, name) ->
                val isSelected = id == selectedId
                ItemSelectionCard(
                    name = name,
                    isSelected = isSelected,
                    iconVector = iconVector,
                    onClick = { onItemSelected(id) },
                    modifier = Modifier.weight(1f, fill = false) // fill=false чтобы не растягивались уродливо
                )
            }

            // Кнопка добавления (+)
            AddItemCard(
                onClick = onCreateClick,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

@Composable
private fun ItemSelectionCard(
    name: String,
    isSelected: Boolean,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (isSelected) 2.dp else 0.dp

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = MaterialTheme.colorScheme.surface,
        border = if (isSelected) BorderStroke(borderWidth, borderColor) else null,
        modifier = modifier
            .height(80.dp)     // Фиксированная высота как на макете
            .widthIn(min = 100.dp) // Минимальная ширина
    ) {
        Box(
            modifier = Modifier.padding(Dimens.ItemCardPadding)
        ) {
            // Название
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Иконка (если есть, например для Хаба)
            if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Индикатор выбранности (опционально, если бордера мало)
            /* if (isSelected) {
               Icon(...)
            }
            */
        }
    }
}

@Composable
private fun AddItemCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .height(80.dp)
            .widthIn(min = 100.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Кружок с плюсиком
            Surface(
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                color = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.add),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}