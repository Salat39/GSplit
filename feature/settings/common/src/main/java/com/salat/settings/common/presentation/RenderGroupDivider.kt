package com.salat.settings.common.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.salat.uikit.component.TopShadow

@Composable
fun RenderGroupDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(12.dp)
    ) {
        TopShadow()
    }
}
