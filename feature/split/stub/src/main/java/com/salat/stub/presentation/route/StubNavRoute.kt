package com.salat.stub.presentation.route

import kotlinx.serialization.Serializable

const val STUB_NAV_ROUTE_NAME = "StubNavRoute"

@Serializable
data class StubNavRoute(val minimizeAfterCloseScreen: Boolean, val toolbarExtraSize: Int)
