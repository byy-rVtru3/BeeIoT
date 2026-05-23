package com.app.mobile.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.app.mobile.R
import com.app.mobile.ui.theme.Dimens

/**
 * Компонент для отображения секции выбора элементов в grid-формате
 * Используется в HiveEditorScreen и QueenEditorScreen
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectionGridSection(
    title: String,
    items: List<Pair<String, String>>, // ID, Name
    selectedId: String?,
    onItemSelected: (String) -> Unit,
    onCreateClick: () -> Unit,
    iconVector: ImageVector? = null
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
    iconVector: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (isSelected) Dimens.BorderWidthNormal else Dimens.Null

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = MaterialTheme.colorScheme.surface,
        border = if (isSelected) BorderStroke(borderWidth, borderColor) else null,
        modifier = modifier
            .height(Dimens.SelectionGridItemHeight)
            .widthIn(min = Dimens.SelectionGridItemMinWidth)
    ) {
        Box(
            modifier = Modifier.padding(Dimens.ItemCardPadding)
        ) {
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
                        .size(Dimens.IconSizeMedium),
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
            .height(Dimens.SelectionGridItemHeight)
            .widthIn(min = Dimens.SelectionGridItemMinWidth)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                shape = CircleShape,
                border = BorderStroke(Dimens.BorderWidthThin, MaterialTheme.colorScheme.primary),
                color = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.add),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(Dimens.TimelineItemSpacing)
                )
            }
        }
    }
}

