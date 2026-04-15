package com.salat.navigation.transitions.entity

data class TransitionRule(
    val enter: String,
    val exit: String,
    val invert: Boolean = false,
    val graph: Boolean = false,
    val type: TransitionType = TransitionType.SLIDE
)
