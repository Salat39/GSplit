package com.salat.settings.common.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

@Composable
fun RenderToolbar(title: String, onNavigateToBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(AppTheme.colors.surfaceBackground),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier
                .size(56.dp)
                .padding(start = 2.dp),
            onClick = onNavigateToBack
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                tint = Color.White,
                contentDescription = "back"
            )
        }
        Spacer(Modifier.width(10.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = AppTheme.colors.contentPrimary,
            style = AppTheme.typography.toolbar,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        Spacer(Modifier.width(10.dp))
    }
}
