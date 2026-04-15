package com.salat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DefaultThreadEffect(key1: Any?, block: suspend CoroutineScope.() -> Unit) {
    LaunchedEffect(key1) {
        withContext(Dispatchers.Default) {
            try {
                block()
            } catch (_: CancellationException) {
            }
        }
    }
}

@Composable
fun IOThreadEffect(key1: Any?, block: suspend CoroutineScope.() -> Unit) {
    LaunchedEffect(key1) {
        withContext(Dispatchers.IO) {
            try {
                block()
            } catch (_: CancellationException) {
            }
        }
    }
}
