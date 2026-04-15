package com.salat.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Collect flow event in compose
@Composable
fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flow, lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onEvent)
            }
        }
    }
}

// Collect flow event without compose
fun <T> ComponentActivity.observeLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) =
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collectLatest(collect)
            }
        }
    }

// Collect flow event without compose
fun <T> ComponentActivity.observeLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) = lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        withContext(Dispatchers.Main.immediate) {
            flow.collect(collect)
        }
    }
}

// Collect flow event without compose
fun <T> Fragment.observeLatestLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) =
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collectLatest(collect)
            }
        }
    }

// Collect flow event without compose
fun <T> Fragment.observeLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) =
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(collect)
            }
        }
    }
