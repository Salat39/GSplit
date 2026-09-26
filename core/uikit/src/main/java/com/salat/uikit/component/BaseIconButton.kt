package com.salat.uikit.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun BaseIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) = Box(
    modifier = modifier
        .minimumInteractiveComponentSize()
        .size(40.dp)
        .clip(CircleShape)
        .clickable(
            onClick = onClick,
            role = Role.Button,
            interactionSource = null,
            indication = ripple(bounded = false, radius = 20.dp)
        ),
    contentAlignment = Alignment.Center
) {
    content()
}
