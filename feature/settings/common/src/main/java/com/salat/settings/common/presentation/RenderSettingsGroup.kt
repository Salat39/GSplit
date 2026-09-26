package com.salat.settings.common.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

private val SettingsGroupShape = RoundedCornerShape(20.dp)

@Composable
fun RenderSettingsGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) = Column(
    modifier = modifier
        .fillMaxWidth()
        .clip(SettingsGroupShape)
        .background(AppTheme.colors.surfaceSettingsLayer1),
    content = content
)
