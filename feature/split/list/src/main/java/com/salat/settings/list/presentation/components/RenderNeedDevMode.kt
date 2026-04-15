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
import presentation.isCarBuildType
import presentation.openAboutDeviceSettings

@Composable
internal fun RenderNeedDevMode(innerPadding: PaddingValues) = Column(
    modifier = Modifier
        .fillMaxSize()
        .background(AppTheme.colors.surfaceBackground)
        .padding(innerPadding),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = stringResource(if (isCarBuildType) R.string.enable_dev_mode_car_guid else R.string.enable_dev_mode_guid),
        modifier = Modifier.padding(horizontal = 20.dp),
        color = AppTheme.colors.contentPrimary,
        style = AppTheme.typography.stubTitle,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(20.dp))

    if (!isCarBuildType) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val context = LocalContext.current
            BaseButton(title = stringResource(R.string.about_phone), onClick = context::openAboutDeviceSettings)
        }
    }
}
