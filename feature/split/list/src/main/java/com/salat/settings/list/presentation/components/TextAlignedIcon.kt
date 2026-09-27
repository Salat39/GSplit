package com.salat.settings.list.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.em
import com.salat.ui.rememberPainterResource

private const val ICON_ID = "icon"

// Height of the icon center above the baseline. It is between the centers of Roboto lowercase and capital letters
private const val ICON_CENTER_EM = .3f

// The icon is an inline character in a text with the style of the adjacent text
// This keeps the icon size and position relative to the text at all UI scales
@Composable
internal fun TextAlignedIcon(
    @DrawableRes iconRes: Int,
    textStyle: TextStyle,
    tint: Color,
    widthEm: Float,
    heightEm: Float,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val painter = rememberPainterResource(iconRes)
    val icon = InlineTextContent(Placeholder(widthEm.em, heightEm.em, PlaceholderVerticalAlign.AboveBaseline)) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = size.height * (.5f - ICON_CENTER_EM / heightEm) }
        )
    }
    Text(
        text = buildAnnotatedString { appendInlineContent(ICON_ID) },
        modifier = modifier.clearAndSetSemantics { contentDescription?.let { this.contentDescription = it } },
        style = textStyle,
        maxLines = 1,
        softWrap = false,
        inlineContent = mapOf(ICON_ID to icon)
    )
}

// Convert the font size first. Nonlinear font scaling uses a different factor for each sp value
@Composable
internal fun TextStyle.emToDp(em: Float) = with(LocalDensity.current) { fontSize.toDp() } * em
