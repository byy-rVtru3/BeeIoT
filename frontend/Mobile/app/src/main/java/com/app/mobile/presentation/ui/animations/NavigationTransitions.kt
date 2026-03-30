package com.app.mobile.presentation.ui.animations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

private const val SlidePercent = 0.08f

val NavEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    fadeIn(tween(MotionSpecs.NormalMs, easing = MotionSpecs.EaseOut)) +
    slideInHorizontally(tween(MotionSpecs.LongMs, easing = MotionSpecs.EaseOut)) {
        (it * SlidePercent).toInt()
    }
}

val NavExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    fadeOut(tween(MotionSpecs.NormalMs, easing = MotionSpecs.EaseIn)) +
    slideOutHorizontally(tween(MotionSpecs.LongMs, easing = MotionSpecs.EaseIn)) {
        -(it * SlidePercent).toInt()
    }
}

val NavPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    fadeIn(tween(MotionSpecs.NormalMs, easing = MotionSpecs.EaseOut)) +
    slideInHorizontally(tween(MotionSpecs.LongMs, easing = MotionSpecs.EaseOut)) {
        -(it * SlidePercent).toInt()
    }
}

val NavPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
    fadeOut(tween(MotionSpecs.NormalMs, easing = MotionSpecs.EaseIn)) +
    slideOutHorizontally(tween(MotionSpecs.LongMs, easing = MotionSpecs.EaseIn)) {
        (it * SlidePercent).toInt()
    }
}
