package com.salat.uikit.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.salat.uikit.theme.AppTheme

@Composable
fun PreviewScreen(darkTheme: Boolean = true, content: @Composable BoxScope.() -> Unit) {
    AppTheme(darkTheme = darkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
