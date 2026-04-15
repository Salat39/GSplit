package com.salat.settings.common.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

@Composable
fun RenderIconMenuDivider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(.8.dp)
            .padding(start = 72.dp)
            .background(AppTheme.colors.surfaceSettings.copy(.4f))
    )
}
