package com.salat.stub.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.salat.ui.clickableNoRipple
import kotlinx.coroutines.delay
import presentation.getActivity

@Composable
internal fun StubScreen(state: StubViewModel.ViewState, onNavigateToBack: () -> Unit = {}) = Scaffold(
    modifier = Modifier
) { innerPadding ->

    var delayBackEvent by remember { mutableStateOf(false) }
    LaunchedEffect(delayBackEvent) {
        if (delayBackEvent) {
            delay(250L)
            onNavigateToBack()
            delayBackEvent = false
        }
    }

    val context = LocalContext.current
    fun onBack() = if (state.minimizeAfterCloseScreen) {
        context.getActivity()?.moveTaskToBack(true)
        delayBackEvent = true
    } else onNavigateToBack()

    LaunchedEffect(state.finishSingleEvent) {
        if (state.finishSingleEvent) onBack()
    }

    // Override system handler
    BackHandler(onBack = ::onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(innerPadding)
            .clickableNoRipple(onClick = ::onBack)
    ) {
        // Toolbar extra space
        val density = LocalDensity.current
        val toolbarHeight = remember(state.toolbarExtraSize) {
            with(density) { state.toolbarExtraSize.toDp() }
        }
        Spacer(Modifier.height(toolbarHeight))

        if (state.darkScreenBackButton) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier
                        .size(56.dp)
                        .padding(start = 2.dp),
                    onClick = ::onBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        tint = Color.White,
                        contentDescription = "back"
                    )
                }
                Spacer(Modifier.width(10.dp))
//
//                Text(
//                    text = stringResource(R.string.touch_to_exit),
//                    modifier = Modifier.weight(1f),
//                    color = AppTheme.colors.contentPrimary.copy(.2f),
//                    style = AppTheme.typography.stubTitle,
//                    overflow = TextOverflow.Ellipsis,
//                    maxLines = 1
//                )
//
//                Spacer(Modifier.width(10.dp))
            }
        }
    }
}
