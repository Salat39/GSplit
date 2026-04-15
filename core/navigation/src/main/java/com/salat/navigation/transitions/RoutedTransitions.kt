package com.salat.navigation.transitions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import com.salat.navigation.common.extractRouteName
import com.salat.navigation.transitions.entity.TransitionKey
import com.salat.navigation.transitions.entity.TransitionRule
import com.salat.navigation.transitions.entity.TransitionType

private const val SLIDE_ANIMATION_DURATION = 140
private const val SCALE_ANIMATION_DURATION = 140
private const val SCALE_ANIMATION_DELAY = 90
private const val FADE_ANIMATION_DURATION = 150
private const val LONG_FADE_ANIMATION_DURATION = 240
private const val PRE_DELAYED_SLIDE_DELAY = 100

fun AnimatedContentTransitionScope<*>.routedEnterTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry
): EnterTransition = interceptTransitionByRule(initial.destination, target.destination, { rule ->
    when (rule.type) {
        TransitionType.NONE -> EnterTransition.None
        TransitionType.SLIDE -> enterSlideTr(rule.invert, false)
        TransitionType.DELAYED_SLIDE -> enterSlideTr(rule.invert, true)
        TransitionType.FADE -> fadeIntoContainer()
        TransitionType.LONG_FADE -> longFadeIntoContainer()
        TransitionType.SCALE -> scaleIntoContainer()
    }
}, {
    enterSlideTr(invert = false, withDelay = false)
})

fun AnimatedContentTransitionScope<*>.routedExitTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry
): ExitTransition = interceptTransitionByRule(initial.destination, target.destination, { rule ->
    when (rule.type) {
        TransitionType.NONE -> ExitTransition.None
        TransitionType.SLIDE -> exitSlideTr(rule.invert, false)
        TransitionType.DELAYED_SLIDE -> exitSlideTr(rule.invert, true)
        TransitionType.FADE -> fadeOutOfContainer()
        TransitionType.LONG_FADE -> longFadeOutOfContainer()
        TransitionType.SCALE -> scaleOutOfContainer()
    }
}, {
    exitSlideTr(invert = false, withDelay = false)
})

fun AnimatedContentTransitionScope<*>.routedPopEnterTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry
): EnterTransition = interceptTransitionByRule(initial.destination, target.destination, { rule ->
    when (rule.type) {
        TransitionType.NONE -> EnterTransition.None
        TransitionType.SLIDE, TransitionType.DELAYED_SLIDE -> enterSlideTr(!rule.invert, false)
        TransitionType.FADE -> fadeIntoContainer()
        TransitionType.LONG_FADE -> longFadeIntoContainer()
        TransitionType.SCALE -> scaleIntoContainer(false)
    }
}, {
    enterSlideTr(invert = true, withDelay = false)
})

fun AnimatedContentTransitionScope<*>.routedPopExitTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry
): ExitTransition = interceptTransitionByRule(initial.destination, target.destination, { rule ->
    when (rule.type) {
        TransitionType.NONE -> ExitTransition.None
        TransitionType.SLIDE, TransitionType.DELAYED_SLIDE -> exitSlideTr(!rule.invert, false)
        TransitionType.FADE -> fadeOutOfContainer()
        TransitionType.LONG_FADE -> longFadeOutOfContainer()
        TransitionType.SCALE -> scaleOutOfContainer()
    }
}, {
    exitSlideTr(invert = true, withDelay = false)
})

/**
 * Compare current transition route with preset rules
 */
private fun <T> interceptTransitionByRule(
    enter: NavDestination,
    exit: NavDestination,
    onFound: (TransitionRule) -> T,
    onDefault: () -> T
): T {
    val graphEnterRoute = enter.parent?.route.orEmpty().extractRouteName()
    val graphExitRoute = exit.parent?.route.orEmpty().extractRouteName()
    val enterRoute = enter.route.orEmpty().extractRouteName()
    val exitRoute = exit.route.orEmpty().extractRouteName()

    // We first try to find a key rule for graph transitions
    val rule = transitionsMap[TransitionKey(graphEnterRoute, graphExitRoute, true)]
        // If not found, look for a rule for the normal transition
        ?: transitionsMap[TransitionKey(enterRoute, exitRoute, false)]
    return rule?.let(onFound) ?: onDefault()
}

/**
 * Slide transition implementation
 */
private fun AnimatedContentTransitionScope<*>.enterSlideTr(invert: Boolean, withDelay: Boolean): EnterTransition {
    val direction = if (invert) {
        AnimatedContentTransitionScope.SlideDirection.End
    } else {
        AnimatedContentTransitionScope.SlideDirection.Start
    }
    return slideIntoContainer(
        towards = direction,
        animationSpec = tween(
            durationMillis = SLIDE_ANIMATION_DURATION,
            delayMillis = if (withDelay) PRE_DELAYED_SLIDE_DELAY else 0
        )
    )
}

private fun AnimatedContentTransitionScope<*>.exitSlideTr(invert: Boolean, withDelay: Boolean): ExitTransition {
    val direction = if (invert) {
        AnimatedContentTransitionScope.SlideDirection.End
    } else {
        AnimatedContentTransitionScope.SlideDirection.Start
    }
    return slideOutOfContainer(
        towards = direction,
        animationSpec = tween(
            durationMillis = SLIDE_ANIMATION_DURATION,
            delayMillis = if (withDelay) PRE_DELAYED_SLIDE_DELAY else 0
        )
    )
}

/**
 * Scale transition implementation
 */
private fun scaleIntoContainer(isIn: Boolean = true, initialScale: Float = if (isIn) 0.9f else 1.1f): EnterTransition =
    scaleIn(
        animationSpec = tween(
            durationMillis = SCALE_ANIMATION_DURATION,
            delayMillis = SCALE_ANIMATION_DELAY
        ),
        initialScale = initialScale
    ) + fadeIn(animationSpec = tween(SCALE_ANIMATION_DURATION, delayMillis = SCALE_ANIMATION_DELAY))

private fun scaleOutOfContainer(isIn: Boolean = true, targetScale: Float = if (isIn) 0.9f else 1.1f): ExitTransition =
    scaleOut(
        animationSpec = tween(
            durationMillis = SCALE_ANIMATION_DURATION,
            delayMillis = SCALE_ANIMATION_DELAY
        ),
        targetScale = targetScale
    ) + fadeOut(tween(delayMillis = SCALE_ANIMATION_DELAY))

/**
 * Fade transition implementation
 */
private fun fadeIntoContainer(): EnterTransition = fadeIn(tween(durationMillis = FADE_ANIMATION_DURATION))

private fun fadeOutOfContainer(): ExitTransition = fadeOut(tween(durationMillis = FADE_ANIMATION_DURATION))

private fun longFadeIntoContainer(): EnterTransition = fadeIn(tween(durationMillis = LONG_FADE_ANIMATION_DURATION))

private fun longFadeOutOfContainer(): ExitTransition = fadeOut(tween(durationMillis = LONG_FADE_ANIMATION_DURATION))
