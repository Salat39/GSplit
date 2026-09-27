package com.salat.settings.list.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.salat.resources.R

private const val GLYPH_HEIGHT_EM = .72f
private const val GLYPH_ASPECT = 18 / 16f

@Composable
internal fun WindowCaptionGlyph(
    textStyle: TextStyle,
    tint: Color,
    modifier: Modifier = Modifier,
    heightEm: Float = GLYPH_HEIGHT_EM
) = TextAlignedIcon(
    iconRes = R.drawable.ic_window_caption,
    textStyle = textStyle,
    tint = tint,
    widthEm = heightEm * GLYPH_ASPECT,
    heightEm = heightEm,
    modifier = modifier,
    contentDescription = stringResource(R.string.window_type_caption)
)
