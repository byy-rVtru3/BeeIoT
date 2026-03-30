package com.app.mobile.presentation.ui.animations

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color

object MotionSpecs {
    const val ShortMs  = 150
    const val NormalMs = 250
    const val LongMs   = 350
    const val ThemeMs  = 400

    val EaseOut   = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val EaseIn    = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val EaseInOut = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

    val SwipeDismiss = tween<Float>(NormalMs, easing = EaseOut)
    val SwipeReturn  = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness    = Spring.StiffnessMediumLow
    )
    val CardPress       = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness    = Spring.StiffnessMedium
    )
    val CardPressColor = tween<Color>(ShortMs, easing = EaseOut)
    val ThemeColor     = tween<Color>(ThemeMs, easing = EaseInOut)
}
