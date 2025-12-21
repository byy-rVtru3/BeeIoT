package com.app.mobile.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import com.app.mobile.R
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