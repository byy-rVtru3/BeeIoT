package com.app.mobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.app.mobile.R
import com.app.mobile.presentation.ui.modifiers.styleShadow
import com.app.mobile.ui.theme.Dimens

sealed interface TopBarAction {
    data class Delete(val onClick: () -> Unit) : TopBarAction
    data class Archive(val onClick: () -> Unit) : TopBarAction
    data class Custom(
        val icon: ImageVector,
        val onClick: () -> Unit,
        val isDestructive: Boolean = false
    ) : TopBarAction
}

@Composable
private fun BaseTopBarContainer(
    hasBackground: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val containerModifier = if (hasBackground) {
        modifier
            .fillMaxWidth()
            .height(Dimens.TopBarHeight)
            .styleShadow()
            .clip(
                RoundedCornerShape(
                    bottomStart = Dimens.BorderRadiusMedium,
                    bottomEnd = Dimens.BorderRadiusMedium
                )
            )
            .background(MaterialTheme.colorScheme.surface)
    } else {
        modifier
            .fillMaxWidth()
            .height(Dimens.TopBarHeight)
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun AppTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasBackground: Boolean = true,
    action: TopBarAction? = null
) {
    BaseTopBarContainer(
        hasBackground = hasBackground,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = Dimens.TopBarHorizontalPadding),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_return),
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(Dimens.TopbarIconSize)
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (action != null) {
            val (icon, onClick, tint) = when (action) {
                is TopBarAction.Delete -> Triple(
                    ImageVector.vectorResource(R.drawable.ic_trash),
                    action.onClick,
                    MaterialTheme.colorScheme.error
                )

                is TopBarAction.Archive -> Triple(
                    ImageVector.vectorResource(R.drawable.ic_trash),
                    action.onClick,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )

                is TopBarAction.Custom -> Triple(
                    action.icon,
                    action.onClick,
                    if (action.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = Dimens.TopBarHorizontalPadding),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(Dimens.TopbarIconSize)
                    )
                }
            }
        }
    }
}

@Composable
fun SelectorTopBar(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hasBackground: Boolean = true
) {
    BaseTopBarContainer(
        hasBackground = hasBackground,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.ScreenContentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = index == selectedTabIndex
                val color =
                    if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.5f
                    )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = Dimens.SelectTopBarHorizontalPadding, vertical = Dimens.SelectTopBarVerticalPadding)
                        .clip(RoundedCornerShape(50))
                        .clickable { onTabSelected(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = color
                    )

                    Spacer(modifier = Modifier.height(Dimens.SelectTopBarPaddingBetweenLine))

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(Dimens.SelectTopBarLineWidth)
                                .height(Dimens.SelectTopBarLineHeight)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(Dimens.SelectTopBarLineHeight))
                    }
                }
            }
        }
    }
}