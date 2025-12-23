package com.app.mobile.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import com.app.mobile.R
import com.app.mobile.presentation.models.queen.QueenPreviewModel
import com.app.mobile.ui.theme.Dimens


val IconActive = Color.Black
val IconInactive = Color.LightGray

@Composable
fun SelectableCardContainer(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()


    val borderColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.outline else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "border_color_anim"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(Dimens.BorderWidthNormal, borderColor),
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
fun HiveItemCard(
    name: String,
    lastConnection: String,
    isSignalActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SelectableCardContainer(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.ItemCardPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.HiveItemPadding)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )




            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.hive_last_connection_format, lastConnection),
                    style = MaterialTheme.typography.bodySmall

                )
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_sensors),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.HiveItemCardIconSize),
                    tint = if (isSignalActive) IconActive else IconInactive
                )
            }
        }
    }
}


@Composable
fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = Dimens.Null
    ) {
        Column(
            modifier = Modifier.padding(Dimens.ItemCardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemCardTextPadding)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

enum class QueenCardDisplayMode {
    SHOW_HIVE,
    Compact
}
@Composable
fun QueenCard(
    queen: QueenPreviewModel,
    onClick: () -> Unit,
    displayMode: QueenCardDisplayMode = QueenCardDisplayMode.SHOW_HIVE
) {
    val isError = queen.stage.isActionRequired
    val statusColor =
        if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    val borderStroke = if (isError) BorderStroke(
        Dimens.BorderWidthNormal,
        MaterialTheme.colorScheme.error
    ) else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = borderStroke
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.ItemCardPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemCardTextPadding)
        ) {

                if (displayMode == QueenCardDisplayMode.SHOW_HIVE) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemCardBigTextPadding)) {
                        Text(
                            text = queen.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Улей: queen.hiveName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = queen.stage.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = queen.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = queen.stage.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }


                LinearProgressIndicator(
                    progress = { queen.stage.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.BorderWidthThick),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = queen.stage.remainingDays,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = queen.stage.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }



@Composable
fun DetailsItemCard(
    title: String,
    description: String,
    footer: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.ItemCardPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemCardBigTextPadding)

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.ItemCardTextPadding)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }



            Text(
                text = footer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            )
        }
    }
}