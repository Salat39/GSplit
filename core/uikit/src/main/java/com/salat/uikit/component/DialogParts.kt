package com.salat.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.salat.uikit.theme.AppTheme

val DialogTextPadding = 24.dp
val DialogContainerPadding = 16.dp
val DialogTopPadding = 24.dp
val DialogInsetShape = RoundedCornerShape(16.dp)

private const val INSET_SHADOW_ALPHA = .35f
private val InsetShadowHeight = 10.dp
private val DialogButtonMinHeight = 48.dp

enum class DialogButtonKind { Neutral, Accent, Danger }

@Composable
fun DialogTitle(text: String) = Text(
    text = text,
    modifier = Modifier.padding(horizontal = DialogTextPadding),
    color = AppTheme.colors.contentPrimary,
    style = AppTheme.typography.dialogTitle,
    overflow = TextOverflow.Ellipsis,
    maxLines = 2
)

@Composable
fun DialogScrollArea(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) = Box(
    modifier = modifier
        .fillMaxWidth()
        .background(AppTheme.colors.surfaceLayer1)
) {
    content()
    TopShadow(alpha = INSET_SHADOW_ALPHA, height = InsetShadowHeight)
    BottomShadow(
        modifier = Modifier.align(Alignment.BottomCenter),
        alpha = INSET_SHADOW_ALPHA,
        height = InsetShadowHeight
    )
}

@Composable
fun DialogButtons(content: @Composable () -> Unit) = FlowRow(
    modifier = Modifier
        .fillMaxWidth()
        .padding(
            start = DialogContainerPadding,
            top = 8.dp,
            end = DialogContainerPadding,
            bottom = DialogContainerPadding
        ),
    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    verticalArrangement = Arrangement.spacedBy(4.dp)
) {
    content()
}

@Composable
fun DialogButton(
    text: String,
    kind: DialogButtonKind = DialogButtonKind.Neutral,
    enabled: Boolean = true,
    onClick: () -> Unit
) = TextButton(
    onClick = onClick,
    modifier = Modifier.heightIn(min = DialogButtonMinHeight),
    enabled = enabled,
    colors = ButtonDefaults.textButtonColors(
        contentColor = if (kind == DialogButtonKind.Danger) {
            AppTheme.colors.deleteButton
        } else {
            AppTheme.colors.settingsTitleAccent
        },
        disabledContentColor = AppTheme.colors.contentPrimary.copy(SettingsDefaults.DISABLED_ALPHA)
    )
) {
    Text(
        text = text,
        style = AppTheme.typography.alertDialogButton,
        textAlign = TextAlign.Center,
        maxLines = 2
    )
}
