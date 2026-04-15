package com.salat.settings.add.presentation.route

import kotlinx.serialization.Serializable

const val SPLIT_ADD_NAV_ROUTE_NAME = "SplitAddNavRoute"

@Serializable
data class SplitAddNavRoute(val editId: Long?, val type: Int?)
