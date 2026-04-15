package com.salat.uikit.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

@Composable
fun RenderSettingsButton(title: String, subtitle: String? = null, enable: Boolean = true, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enable) {
                onClick()
            }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(1f)
                .then(if (!enable) Modifier.graphicsLayer { alpha = .25f } else Modifier)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 23.dp),
                text = title,
                style = AppTheme.typography.screenTitle,
                color = AppTheme.colors.contentPrimary
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = subtitle ?: "",
                modifier = Modifier.padding(horizontal = 23.dp),
                color = AppTheme.colors.contentPrimary.copy(.4f),
                style = AppTheme.typography.dialogSubtitle
            )
        }
        Spacer(Modifier.width(20.dp))
    }
}
