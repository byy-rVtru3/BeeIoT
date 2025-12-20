package com.app.mobile.presentation.ui.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import com.app.mobile.ui.theme.Dimens

fun Modifier.styleShadow(
    color: Color = Color.Black,
    alpha: Float = 0.1f,
    blur: Dp = Dimens.ShadowBlur,
    borderRadius: Dp = Dimens.Null,
    offsetX: Dp = Dimens.Null,
    offsetY: Dp = Dimens.Null,
    spread: Dp = Dimens.Null
) = this.drawBehind {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    val transparentColor = color.copy(alpha = 0f).toArgb()

    this.drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor

        frameworkPaint.setShadowLayer(
            blur.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )

        it.drawRoundRect(
            left = 0f - spread.toPx(),
            top = 0f - spread.toPx(),
            right = size.width + spread.toPx(),
            bottom = size.height + spread.toPx(),
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint = paint
        )
    }
}