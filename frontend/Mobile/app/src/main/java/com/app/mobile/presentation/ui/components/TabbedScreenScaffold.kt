package com.app.mobile.presentation.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Переиспользуемый Scaffold с вкладками и опциональным FAB
 *
 * @param tabs Список названий вкладок
 * @param selectedTabIndex Индекс выбранной вкладки
 * @param onTabSelected Callback при выборе вкладки
 * @param modifier Модификатор
 * @param showFabOnTab Индекс вкладки, на которой показывать FAB (null - не показывать)
 * @param fabIcon Иконка для FAB
 * @param fabContentDescription Описание FAB для accessibility
 * @param onFabClick Callback при клике на FAB
 * @param content Содержимое экрана
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabbedScreenScaffold(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    showFabOnTab: Int? = 0,
    fabIcon: ImageVector? = null,
    fabContentDescription: String? = null,
    onFabClick: (() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        topBar = {
            SelectorTopBar(
                tabs = tabs,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = onTabSelected
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.navigationBars),
        floatingActionButton = {
            if (selectedTabIndex == showFabOnTab && fabIcon != null && onFabClick != null) {
                CustomFloatingActionButton(
                    onClick = onFabClick,
                    icon = fabIcon,
                    contentDescription = fabContentDescription ?: ""
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}
