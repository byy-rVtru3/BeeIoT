package com.app.mobile.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import com.app.mobile.ui.theme.Dimens

data class HowToUseAccordionSection(
    val title: String,
    val body: String,
    val showStepNumbers: Boolean = true
)

@Composable
fun HowToUseAccordion(
    sections: List<HowToUseAccordionSection>,
    modifier: Modifier = Modifier,
    initiallyExpandedIndex: Int = 0
) {
    val normalizedInitialIndex = initiallyExpandedIndex.coerceIn(-1, sections.lastIndex)
    var expandedSectionIndex by rememberSaveable(sections.size) { mutableIntStateOf(normalizedInitialIndex) }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingSmallMedium)
    ) {
        itemsIndexed(sections) { index, section ->
            HowToUseAccordionItem(
                title = section.title,
                body = section.body,
                showStepNumbers = section.showStepNumbers,
                expanded = expandedSectionIndex == index,
                onHeaderClick = {
                    expandedSectionIndex = if (expandedSectionIndex == index) -1 else index
                }
            )
        }
    }
}

@Composable
private fun HowToUseAccordionItem(
    title: String,
    body: String,
    showStepNumbers: Boolean,
    expanded: Boolean,
    onHeaderClick: () -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "accordion_arrow_rotation"
    )

    val steps = remember(body) {
        body
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map(::normalizeStepText)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.BorderRadiusMedium),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onHeaderClick)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = Dimens.Size12, vertical = Dimens.Size10),
                horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingSmallMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )

                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.rotate(arrowRotation)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Dimens.Size12, end = Dimens.Size12, bottom = Dimens.Size12)
                ) {
                    steps.forEachIndexed { index, stepText ->
                        HowToUseStepItem(
                            stepNumber = "${index + 1}",
                            text = stepText,
                            isLast = index == steps.lastIndex,
                            showStepNumbers = showStepNumbers
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HowToUseStepItem(
    stepNumber: String,
    text: String,
    isLast: Boolean,
    showStepNumbers: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Size8),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(Dimens.BorderWidthNormal, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            ) {
                Box(
                    modifier = Modifier.size(Dimens.Size24),
                    contentAlignment = Alignment.Center
                ) {
                    if (showStepNumbers) {
                        Text(
                            text = stepNumber,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(Dimens.Size8)
                                .background(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = Dimens.Size4)
                        .width(Dimens.BorderWidthNormal)
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                )
            }
        }

        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) Dimens.Null else Dimens.Size8),
            shape = RoundedCornerShape(Dimens.Size8),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(Dimens.BorderWidthNormal, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(horizontal = Dimens.Size10, vertical = Dimens.Size8)
            )
        }
    }
}

private fun normalizeStepText(rawText: String): String {
    return rawText
        .replace(Regex("^\\d+\\)\\s*"), "")
        .replace(Regex("^-\\s*"), "")
        .trim()
}
