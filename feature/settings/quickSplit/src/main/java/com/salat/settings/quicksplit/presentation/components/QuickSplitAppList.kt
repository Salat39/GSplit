package com.salat.settings.quicksplit.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.quicksplit.presentation.entity.DisplayQuickSplitApp
import com.salat.uikit.component.DialogAppIconShape
import com.salat.uikit.component.DialogAppList
import com.salat.uikit.theme.AppTheme

// Null apps show the progress of the installed apps scan
@Composable
internal fun ColumnScope.QuickSplitAppList(
    apps: List<DisplayQuickSplitApp>?,
    onClick: (DisplayQuickSplitApp) -> Unit,
    isSelected: (DisplayQuickSplitApp) -> Boolean = { false }
) {
    if (apps == null) {
        RenderScan()
        return
    }

    DialogAppList(
        items = apps,
        title = { it.appName },
        subtitle = { it.packageName },
        isSelected = isSelected,
        onClick = onClick,
        icon = { app ->
            DrawableImage(
                drawable = app.icon,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(DialogAppIconShape)
            )
        }
    )
}

@Composable
private fun RenderScan() = Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    CircularProgressIndicator(
        modifier = Modifier.size(36.dp),
        color = AppTheme.colors.contentPrimary
    )
    Spacer(Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.scanning_installed_apps),
        color = AppTheme.colors.contentPrimary,
        textAlign = TextAlign.Center
    )
}
