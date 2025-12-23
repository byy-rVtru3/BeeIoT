package com.app.mobile.presentation.ui.screens.queen.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.queen.QueenEditorModel
import com.app.mobile.presentation.ui.components.*
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorNavigationEvent
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorUiState
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorViewModel
import com.app.mobile.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QueenEditorScreen(
    queenEditorViewModel: QueenEditorViewModel,
    onBackClick: () -> Unit
) {
    val queenUiState by queenEditorViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        queenEditorViewModel.loadQueen()
    }

    ObserveAsEvents(queenEditorViewModel.event) { event ->
        when (event) {
            is QueenEditorNavigationEvent.NavigateBack -> onBackClick()
        }
    }

    when (val state = queenUiState) {
        is QueenEditorUiState.Loading -> FullScreenProgressIndicator()
        is QueenEditorUiState.Error -> ErrorMessage(state.message, onRetry = {})
        is QueenEditorUiState.Content -> {
            QueenEditorContent(
                queenEditorModel = state.queenEditorModel,
                onNameChange = queenEditorViewModel::onNameChange,
                onDateChange = queenEditorViewModel::onDateChange,
                onHiveAdd = queenEditorViewModel::addHive,
                onSaveClick = queenEditorViewModel::onSaveClick,
                onBackClick = onBackClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueenEditorContent(
    queenEditorModel: QueenEditorModel,
    onNameChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onHiveAdd: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.queen_edit_title), // "Добавление/Редактирование матки"
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

            // --- Поле ввода имени ---
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)) {
                Text(
                    text = stringResource(R.string.queen_name_label), // "Имя матки"
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CustomTextField(
                    value = queenEditorModel.name,
                    onValueChange = onNameChange,
                    placeholder = stringResource(R.string.queen_name_placeholder),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- Выбор даты рождения ---
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = queenEditorModel.birthDate)
            var showDatePicker by remember { mutableStateOf(false) }
            val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)) {
                Text(
                    text = stringResource(R.string.birth_date_label), // "Дата рождения"
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    CustomTextField(
                        value = dateFormatter.format(Date(queenEditorModel.birthDate)),
                        onValueChange = {},
                        placeholder = "",
                        trailingIcon = {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Прозрачный слой поверх для клика
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { onDateChange(it) }
                            showDatePicker = false
                        }) {
                            Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // --- Секция выбора улья (Grid) ---
            // Используем тот же компонент, что и в HiveEditorScreen
            SelectionGridSection(
                title = stringResource(R.string.available_hives), // "Доступные ульи"
                items = queenEditorModel.hives.map { it.id to it.name },
                selectedId = queenEditorModel.hiveId,
                onItemSelected = onHiveAdd,
                onCreateClick = {
                    // Логика создания улья отсюда пока не предусмотрена
                    // Можно добавить навигацию или оставить пустым
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = stringResource(R.string.save),
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


// --- Копия компонентов из HiveEditorScreen для автономности этого файла ---
// В реальном проекте лучше вынести их в общий файл (например, SelectionComponents.kt)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectionGridSection(
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

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
        ) {
            items.forEach { (id, name) ->
                val isSelected = id == selectedId
                ItemSelectionCard(
                    name = name,
                    isSelected = isSelected,
                    iconVector = iconVector,
                    onClick = { onItemSelected(id) },
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

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
            .height(80.dp)
            .widthIn(min = 100.dp)
    ) {
        Box(modifier = Modifier.padding(Dimens.ItemCardPadding)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.TopStart)
            )

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