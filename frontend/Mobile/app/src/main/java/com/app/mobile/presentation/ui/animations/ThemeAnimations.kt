package com.app.mobile.presentation.ui.animations

import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ColorScheme.animated(): ColorScheme {
    @Composable
    fun Color.anim() = animateColorAsState(this, MotionSpecs.ThemeColor, label = "").value
    return copy(
        primary                = primary.anim(),
        onPrimary              = onPrimary.anim(),
        primaryContainer       = primaryContainer.anim(),
        onPrimaryContainer     = onPrimaryContainer.anim(),
        secondary              = secondary.anim(),
        onSecondary            = onSecondary.anim(),
        secondaryContainer     = secondaryContainer.anim(),
        onSecondaryContainer   = onSecondaryContainer.anim(),
        tertiary               = tertiary.anim(),
        onTertiary             = onTertiary.anim(),
        tertiaryContainer      = tertiaryContainer.anim(),
        onTertiaryContainer    = onTertiaryContainer.anim(),
        background             = background.anim(),
        onBackground           = onBackground.anim(),
        surface                = surface.anim(),
        onSurface              = onSurface.anim(),
        surfaceVariant         = surfaceVariant.anim(),
        onSurfaceVariant       = onSurfaceVariant.anim(),
        outline                = outline.anim(),
        outlineVariant         = outlineVariant.anim(),
        error                  = error.anim(),
        onError                = onError.anim(),
        errorContainer         = errorContainer.anim(),
        onErrorContainer       = onErrorContainer.anim(),
        inverseOnSurface       = inverseOnSurface.anim(),
        inversePrimary         = inversePrimary.anim(),
        inverseSurface         = inverseSurface.anim(),
        scrim                  = scrim.anim(),
        surfaceTint            = surfaceTint.anim(),
    )
}
