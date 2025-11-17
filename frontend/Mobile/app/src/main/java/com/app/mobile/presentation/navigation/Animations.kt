package com.app.mobile.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

private const val FADE_DURATION = 200
private const val SLIDE_DURATION = 300
private const val OFFSET_PERCENT = 0.15

val ENTER_TRANSITION: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
    {
        fadeIn(
            tween(FADE_DURATION),
            initialAlpha = 0.5f
        ) + slideIn(tween(SLIDE_DURATION)) {
            IntOffset(
                x = (it.width * OFFSET_PERCENT).toInt(),
                y = 0
            )
        }
    }
val EXIT_TRANSITION: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
    {
        fadeOut(tween(FADE_DURATION)) + slideOut(tween(SLIDE_DURATION)) {
            IntOffset(
                x = -(it.width * OFFSET_PERCENT).toInt(),
                y = 0
            )
        }
    }
val POP_ENTER_TRANSITION: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
    {
        fadeIn(
            tween(FADE_DURATION),
            initialAlpha = 0.5f
        ) + slideIn(tween(SLIDE_DURATION)) {
            IntOffset(
                x = -(it.width * OFFSET_PERCENT).toInt(),
                y = 0
            )
        }
    }
val POP_EXIT_TRANSITION: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
    {
        fadeOut(tween(FADE_DURATION)) + slideOut(tween(SLIDE_DURATION)) {
            IntOffset(
                x = (it.width * OFFSET_PERCENT).toInt(),
                y = 0
            )
        }
    }