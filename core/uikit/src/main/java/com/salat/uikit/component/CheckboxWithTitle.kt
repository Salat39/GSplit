package com.salat.uikit.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

@Composable
fun CheckboxWithTitle(
    modifier: Modifier = Modifier,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .then(modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(
                    color = Color.Black.copy(alpha = 0.3f),
                    bounded = true
                ),
                onClick = { onCheckedChange(!checked) }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTheme.typography.radioTitle,
            color = AppTheme.colors.contentPrimary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Checkbox(
            checked = checked,
            enabled = true,
            colors = CheckboxColors(
                checkedCheckmarkColor = Color.White,
                uncheckedCheckmarkColor = Color.Transparent,
                checkedBoxColor = Color.Black.copy(.2f),
                uncheckedBoxColor = Color.Black.copy(.2f),
                disabledCheckedBoxColor = Color.Black.copy(.2f),
                disabledUncheckedBoxColor = Color.Black.copy(.2f),
                disabledIndeterminateBoxColor = Color.Black.copy(.2f),
                checkedBorderColor = Color.Black.copy(.2f),
                uncheckedBorderColor = Color.Black.copy(.2f),
                disabledBorderColor = Color.Black.copy(.2f),
                disabledUncheckedBorderColor = Color.Black.copy(.2f),
                disabledIndeterminateBorderColor = Color.Black.copy(.2f),
            ),
            onCheckedChange = null
        )
    }
}
