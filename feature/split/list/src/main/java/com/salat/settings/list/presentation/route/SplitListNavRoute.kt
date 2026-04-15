package com.salat.settings.list.presentation.route

import kotlinx.serialization.Serializable

const val SPLIT_LIST_NAV_ROUTE_NAME = "SplitListNavRoute"

@Serializable
data class SplitListNavRoute(
    val toolbarExtraSize: Int
)
