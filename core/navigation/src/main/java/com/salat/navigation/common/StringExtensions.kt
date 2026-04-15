package com.salat.navigation.common

/**
 * Prepare TypeSafe route name
 */
fun String.extractRouteName(): String {
    val cleanedRoute = substringBefore('/')
    return cleanedRoute.substringAfterLast('.')
}
