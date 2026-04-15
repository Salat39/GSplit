package com.salat.settings.add.presentation.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.uikit.theme.AppTheme

@Composable
internal fun RenderToolbar(
    isEdit: Boolean,
    onNavigateToBack: () -> Unit,
    showApply: State<Boolean>,
    onApply: () -> Unit
) {
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
            text = stringResource(if (isEdit) R.string.editing_a_preset else R.string.creating_a_preset),
            modifier = Modifier.weight(1f),
            color = AppTheme.colors.contentPrimary,
            style = AppTheme.typography.toolbar,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        Spacer(Modifier.width(10.dp))

        androidx.compose.animation.AnimatedVisibility(
            visible = showApply.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IconButton(
                modifier = Modifier
                    .size(56.dp),
                onClick = onApply
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    tint = Color.White,
                    contentDescription = "apply"
                )
            }
        }

        Spacer(Modifier.width(16.dp))
    }
}
