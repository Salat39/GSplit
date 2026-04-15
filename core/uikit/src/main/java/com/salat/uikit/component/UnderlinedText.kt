package com.salat.uikit.component

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import presentation.isRtlText
import timber.log.Timber

private const val UNDERLINE_TAG = "underline"
private const val IGNORE_ANNOTATION = "ignored"

/**
 * Highlights a part of the text with underline.
 * Use buildAnnotatedString{ underline {append(..)} } to highlight the text to be emphasized
 */
@Composable
fun UnderlinedText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    style: TextStyle = LocalTextStyle.current,
    underlineColor: Color = Color.White,
    underlineWidth: Dp = 1.dp,
    underlineShift: Dp = 0.dp
) {
    var onDraw: DrawScope.() -> Unit by remember { mutableStateOf({}) }
    val isRtlContext = LocalLayoutDirection.current == LayoutDirection.Rtl
    val isRtlText by remember {
        derivedStateOf { text.text.isRtlText() }
    }

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        style = style,
        modifier = modifier.drawBehind { onDraw() },
        onTextLayout = { layoutResult ->
            val annotations = text.getStringAnnotations(UNDERLINE_TAG, 0, text.length)
            val bonds = mutableMapOf<Float, SegmentRenderBond>() // bottom -> left, right

            // calc bonds
            for (annotation in annotations) {
                for (i in annotation.start..<annotation.end) {
                    val charBound = layoutResult.getBoundingBox(i)
                    val left = charBound.left
                    val right = charBound.right

                    val current = bonds[charBound.bottom] ?: SegmentRenderBond(
                        if (isRtlContext || isRtlText) right else left,
                        if (isRtlContext || isRtlText) left else right
                    )
                    if (isRtlContext) {
                        if (isRtlText) {
                            bonds[charBound.bottom] = current.copy(
                                left = maxOf(current.left, right),
                                right = minOf(current.right, left)
                            )
                        } else {
                            bonds[charBound.bottom] = current.copy(
                                left = maxOf(right, current.left),
                            )
                        }
                    } else {
                        if (isRtlText) {
                            bonds[charBound.bottom] = current.copy(
                                right = minOf(current.right, left)
                            )
                        } else {
                            bonds[charBound.bottom] = current.copy(
                                left = maxOf(current.left, right),
                                right = minOf(current.right, left)
                            )
                        }
                    }
                }
            }

            // drawing
            if (bonds.isNotEmpty()) {
                onDraw = {
                    val strokeWidthPx = underlineWidth.toPx()
                    val underlinePaddingPx = underlineShift.toPx()
                    for ((bottom, bond) in bonds) {
                        drawLine(
                            color = underlineColor,
                            strokeWidth = strokeWidthPx,
                            start = Offset(bond.left, bottom + underlinePaddingPx),
                            end = Offset(bond.right, bottom + underlinePaddingPx)
                        )
                    }
                }
            }
        }
    )
}

/**
 * This Text can display mixed text of RTL languages and LTR languages. Requires a large overhead to render.
 * Use buildAnnotatedString{ underline {append(..)} } to highlight the text to be emphasized
 */
/*@Composable
fun UnderlinedCompatText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    style: TextStyle = LocalTextStyle.current,
    underlineColor: Color = AppTheme.colors.mono.contentAccent,
    underlineWidth: Dp = 1.dp,
    underlineShift: Dp = 0.dp
) {
    var onDraw: DrawScope.() -> Unit by remember { mutableStateOf({}) }

    Text(text = text,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        style = style,
        modifier = modifier.drawBehind { onDraw() },
        onTextLayout = { layoutResult ->
            val annotations = text.getStringAnnotations(UNDERLINE_ANNOTATION_TAG, 0, text.length)

            // drawing
            onDraw = {
                for (annotation in annotations) {
                    for (i in annotation.start..<annotation.end) {
                        val charBound = layoutResult.getBoundingBox(i)

                        val strokeWidthPx = underlineWidth.toPx()
                        val underlinePaddingPx = underlineShift.toPx()
                        drawLine(
                            color = underlineColor,
                            strokeWidth = strokeWidthPx,
                            start = Offset(charBound.left, charBound.bottom + underlinePaddingPx),
                            end = Offset(charBound.right, charBound.bottom + underlinePaddingPx)
                        )
                    }
                }
            }
        })
}*/

/**
 * Highlights a part of the text with underline. Catches clicks on underlined text.
 * Use buildAnnotatedString{ underline {append(..)} } to highlight the text to be emphasized
 * To prepare HTML text for clicks, use: text.fromHtml().toAnnotatedString()
 */
@Composable
fun ClickableUnderlinedText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    underlineColor: Color = Color.White,
    underlineWidth: Dp = 1.dp,
    underlineShift: Dp = 0.dp,
    textClick: (() -> Unit)? = null,
    urlClick: (String) -> Unit,
) {
    // Variable to hold custom drawing lambda for underlines
    var onDraw: DrawScope.() -> Unit by remember { mutableStateOf({}) }
    val isRtlContext = LocalLayoutDirection.current == LayoutDirection.Rtl
    val isRtlText by remember { derivedStateOf { text.text.isRtlText() } }

    // State to hold the latest text layout result
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = text,
        style = style.copy(color = color),
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        modifier = modifier
            // Detect tap gestures and calculate the clicked text offset
            .pointerInput(textClick) {
                detectTapGestures { tapOffset ->
                    layoutResult?.let { result ->
                        val position = result.getOffsetForPosition(tapOffset)
                        // Check if the clicked offset is within any annotation range
                        text.getStringAnnotations(0, text.length).forEach { annotation ->
                            if (position in annotation.start..annotation.end &&
                                annotation.item != IGNORE_ANNOTATION
                            ) {
                                if (annotation.tag == UNDERLINE_TAG) {
                                    urlClick(annotation.item)
                                    return@detectTapGestures
                                }
                            }
                        }
                        textClick?.invoke()
                    }
                }
            }
            // Draw custom underlines behind the text
            .drawBehind { onDraw() },
        // Capture the TextLayoutResult to calculate bounds for underlined segments
        onTextLayout = { result ->
            layoutResult = result
            val annotations = text.getStringAnnotations(UNDERLINE_TAG, 0, text.length)
            val bonds = mutableMapOf<Float, SegmentRenderBond>() // Map: bottom -> (left, right) bounds

            // Calculate bounds for each annotated segment
            for (annotation in annotations) {
                for (i in annotation.start until annotation.end) {
                    val charBound = result.getBoundingBox(i)
                    val left = charBound.left
                    val right = charBound.right

                    val current = bonds[charBound.bottom] ?: SegmentRenderBond(
                        if (isRtlContext || isRtlText) right else left,
                        if (isRtlContext || isRtlText) left else right
                    )
                    if (isRtlContext) {
                        if (isRtlText) {
                            bonds[charBound.bottom] = current.copy(
                                left = maxOf(current.left, right),
                                right = minOf(current.right, left)
                            )
                        } else {
                            bonds[charBound.bottom] = current.copy(
                                left = maxOf(right, current.left)
                            )
                        }
                    } else {
                        if (isRtlText) {
                            bonds[charBound.bottom] = current.copy(
                                right = minOf(current.right, left)
                            )
                        } else {
                            bonds[charBound.bottom] = current.copy(
                                left = maxOf(current.left, right),
                                right = minOf(current.right, left)
                            )
                        }
                    }
                }
            }

            // Setup the drawing lambda if underlined segments exist
            if (bonds.isNotEmpty()) {
                onDraw = {
                    val strokeWidthPx = underlineWidth.toPx()
                    val underlinePaddingPx = underlineShift.toPx()
                    for ((bottom, bond) in bonds) {
                        drawLine(
                            color = underlineColor,
                            strokeWidth = strokeWidthPx,
                            start = Offset(bond.left, bottom + underlinePaddingPx),
                            end = Offset(bond.right, bottom + underlinePaddingPx)
                        )
                    }
                }
            }
        }
    )
}

fun Spanned.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    val spanned = this@toAnnotatedString
    append(spanned.toString())
    getSpans(0, spanned.length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)
        when (span) {
            is StyleSpan -> when (span.style) {
                Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                Typeface.BOLD_ITALIC -> addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                    start,
                    end
                )
            }

            is UnderlineSpan -> {
                addStringAnnotation(UNDERLINE_TAG, "#$UNDERLINE_TAG", start, end)
            }

            is URLSpan -> {
                addStringAnnotation(UNDERLINE_TAG, span.url.toString(), start, end)
            }

            is ForegroundColorSpan -> addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
            else -> Timber.d("Unknown text span: ${span::class.java}")
        }
    }
}

@OptIn(ExperimentalTextApi::class)
fun AnnotatedString.Builder.underline(
    annotation: String = IGNORE_ANNOTATION,
    action: AnnotatedString.Builder.() -> Unit
): AnnotatedString.Builder {
    withAnnotation(UNDERLINE_TAG, annotation = annotation) { action() }
    return this
}

@OptIn(ExperimentalTextApi::class)
fun AnnotatedString.Builder.appendUnderline(
    annotation: String = IGNORE_ANNOTATION,
    text: String
): AnnotatedString.Builder {
    withAnnotation(UNDERLINE_TAG, annotation = annotation) { append(text) }
    return this
}

@Immutable
data class SegmentRenderBond(val left: Float, val right: Float)
