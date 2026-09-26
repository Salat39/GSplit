package com.salat.settings.add.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.ui.scaledWithLayout
import com.salat.uikit.component.BaseIconButton
import com.salat.uikit.theme.AppTheme

private const val DOCK_BUTTON_SIZE = 48
private const val DOCK_DIVIDER_LENGTH = 28

private val saveButtonColor = Color(0xFF43A047)

@Composable
internal fun FreeModeDock(
    isVertical: Boolean,
    canSave: Boolean,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerModifier = modifier
        .shadow(8.dp, CircleShape)
        .clip(CircleShape)
        .background(AppTheme.colors.surfaceMenu)
        .padding(4.dp)

    if (isVertical) {
        Column(
            modifier = containerModifier,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SaveButton(enabled = canSave, onClick = onSave)
            FilledTonalIconButton(
                onClick = onAdd,
                modifier = Modifier.size(DOCK_BUTTON_SIZE.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = AppTheme.colors.contentAccent,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.add))
            }
            DockDivider(
                Modifier
                    .width(DOCK_DIVIDER_LENGTH.dp)
                    .height(1.dp)
            )
            BackButton(onClick = onBack)
        }
    } else {
        Row(
            modifier = containerModifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = onBack)
            DockDivider(
                Modifier
                    .width(1.dp)
                    .height(DOCK_DIVIDER_LENGTH.dp)
            )
            FilledTonalButton(
                onClick = onAdd,
                modifier = Modifier.height(DOCK_BUTTON_SIZE.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = AppTheme.colors.contentAccent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(start = 14.dp, end = 18.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(text = stringResource(R.string.add), style = AppTheme.typography.settingsTitle.scaledWithLayout())
            }
            SaveButton(enabled = canSave, onClick = onSave)
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) = BaseIconButton(
    onClick = onClick,
    modifier = Modifier.size(DOCK_BUTTON_SIZE.dp)
) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "back",
        tint = Color.White
    )
}

@Composable
private fun SaveButton(enabled: Boolean, onClick: () -> Unit) = FilledIconButton(
    onClick = onClick,
    enabled = enabled,
    modifier = Modifier.size(DOCK_BUTTON_SIZE.dp),
    colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = saveButtonColor,
        contentColor = Color.White,
        disabledContainerColor = Color.White.copy(.12f),
        disabledContentColor = Color.White.copy(.38f)
    )
) {
    Icon(imageVector = Icons.Filled.Check, contentDescription = "save")
}

@Composable
private fun DockDivider(modifier: Modifier) = Box(modifier.background(Color.White.copy(.14f)))
