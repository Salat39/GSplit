package com.salat.settings.list.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.uikit.component.BaseButton
import com.salat.uikit.theme.AppTheme
import presentation.openOverlayPermissionSettings

@Composable
internal fun RenderNeedDrawOverlay(innerPadding: PaddingValues) = Column(
    modifier = Modifier
        .fillMaxSize()
        .background(AppTheme.colors.surfaceBackground)
        .padding(innerPadding),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = stringResource(R.string.window_overlap_msg),
        modifier = Modifier.padding(horizontal = 20.dp),
        color = AppTheme.colors.contentPrimary,
        style = AppTheme.typography.stubTitle,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(20.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        val context = LocalContext.current
        BaseButton(title = stringResource(R.string.settings), onClick = context::openOverlayPermissionSettings)
    }
}
