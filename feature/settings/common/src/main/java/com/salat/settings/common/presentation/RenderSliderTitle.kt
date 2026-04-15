package com.salat.settings.common.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

@Composable
fun RenderSliderTitle(title: String) {
    Text(
        modifier = Modifier.padding(start = 24.dp, end = 20.dp, top = 18.dp),
        text = title,
        style = AppTheme.typography.screenTitle,
        color = AppTheme.colors.contentPrimary
    )

    Spacer(Modifier.height(6.dp))
}
