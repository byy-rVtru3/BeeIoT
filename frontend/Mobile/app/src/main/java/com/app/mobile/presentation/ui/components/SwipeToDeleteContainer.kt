package com.app.mobile.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.app.mobile.R
import com.app.mobile.presentation.ui.animations.MotionSpecs
import com.app.mobile.ui.theme.Dimens
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val DISMISS_THRESHOLD = 0.45f

/**
 * @param onSwipeToEnd  action on right swipe; null disables right swipe
 * @param onSwipeToStart action on left swipe; null disables left swipe
 * @param endIcon       icon shown during right swipe
 * @param startIcon     icon shown during left swipe
 * @param endColor      background color for right swipe
 * @param startColor    background color for left swipe
 */
@Composable
fun SwipeToDeleteContainer(
    onSwipeToStart: (() -> Unit)? = null,
    onSwipeToEnd: (() -> Unit)? = null,
    startIcon: ImageVector? = null,
    endIcon: ImageVector? = null,
    startColor: Color? = null,
    endColor: Color? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val enableSwipeToEnd = onSwipeToEnd != null
    val enableSwipeToStart = onSwipeToStart != null

    var itemWidth by remember { mutableIntStateOf(0) }
    val offsetX = remember { Animatable(0f) }
    var dismissDirection by remember { mutableStateOf<DismissDirection?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(dismissDirection) {
        if (dismissDirection == DismissDirection.End) onSwipeToEnd?.invoke()
        if (dismissDirection == DismissDirection.Start) onSwipeToStart?.invoke()
    }

    val progress = if (itemWidth > 0) (abs(offsetX.value) / itemWidth).coerceIn(0f, 1f) else 0f
    val isSwipingRight = offsetX.value > 1f
    val isSwipingLeft = offsetX.value < -1f

    val errorColor = MaterialTheme.colorScheme.error
    val onErrorColor = MaterialTheme.colorScheme.onError

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { itemWidth = it.width }
    ) {
        if (isSwipingRight || isSwipingLeft) {
            val iconAlignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd
            val bgAlpha = (progress / DISMISS_THRESHOLD).coerceIn(0f, 1f)
            val iconScale = 0.6f + bgAlpha * 0.4f

            val bgColor = if (isSwipingRight) {
                (endColor ?: errorColor).copy(alpha = bgAlpha)
            } else {
                (startColor ?: errorColor).copy(alpha = bgAlpha)
            }
            val icon = if (isSwipingRight) {
                endIcon ?: ImageVector.vectorResource(R.drawable.ic_trash)
            } else {
                startIcon ?: ImageVector.vectorResource(R.drawable.ic_trash)
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(Dimens.ItemCardRadius))
                    .background(bgColor),
                contentAlignment = iconAlignment
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = onErrorColor.copy(alpha = bgAlpha),
                    modifier = Modifier
                        .padding(horizontal = Dimens.ItemCardPadding)
                        .size(Dimens.HiveItemCardIconSize)
                        .scale(iconScale)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(enableSwipeToStart, enableSwipeToEnd) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                val thresholdPx = itemWidth * DISMISS_THRESHOLD
                                when {
                                    offsetX.value >= thresholdPx && enableSwipeToEnd -> {
                                        offsetX.animateTo(
                                            targetValue = itemWidth.toFloat() + 64f,
                                            animationSpec = MotionSpecs.SwipeDismiss
                                        )
                                        dismissDirection = DismissDirection.End
                                    }
                                    offsetX.value <= -thresholdPx && enableSwipeToStart -> {
                                        offsetX.animateTo(
                                            targetValue = -(itemWidth.toFloat() + 64f),
                                            animationSpec = MotionSpecs.SwipeDismiss
                                        )
                                        dismissDirection = DismissDirection.Start
                                    }
                                    else -> {
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = MotionSpecs.SwipeReturn
                                        )
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = MotionSpecs.SwipeReturn
                                )
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                val newOffset = offsetX.value + dragAmount
                                val clamped = when {
                                    newOffset > 0f && !enableSwipeToEnd   -> 0f
                                    newOffset < 0f && !enableSwipeToStart -> 0f
                                    else                                   -> newOffset
                                }
                                offsetX.snapTo(clamped)
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

private enum class DismissDirection { Start, End }
