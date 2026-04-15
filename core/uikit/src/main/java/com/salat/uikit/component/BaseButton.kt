package com.salat.uikit.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

@Composable
fun BaseButton(modifier: Modifier = Modifier, title: String, onClick: () -> Unit) = Button(
    modifier = modifier,
    colors = ButtonColors(
        containerColor = AppTheme.colors.contentAccent,
        contentColor = AppTheme.colors.contentPrimary,
        disabledContainerColor = Color.Black.copy(.4f),
        disabledContentColor = AppTheme.colors.contentPrimary
    ),
    shape = RoundedCornerShape(8.dp),
    onClick = onClick
) {
    Text(
        modifier = Modifier.padding(4.dp),
        text = title,
        color = AppTheme.colors.contentPrimary,
        style = AppTheme.typography.buttonTitle,
        textAlign = TextAlign.Center
    )
}
