package com.salat.settings.api.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salat.settings.common.presentation.RenderSettingsGroup
import com.salat.uikit.component.toAnnotatedString
import com.salat.uikit.theme.AppTheme
import presentation.spannedFromHtml

private val sectionTitlePattern = Regex("^\\s*<strong>(.*?)</strong>")
private val leadingBreaksPattern = Regex("^(\\s|<br>)+")
private val actionPattern = Regex("com\\.salat\\.gsplit(\\.[A-Za-z0-9_]+)?")
private val quotedNamePattern = Regex("(?<=\")[a-z][a-z0-9_]*(?=\")")
private val assignedNamePattern = Regex("\\b[a-z][a-z0-9_]*(?=\\s*=)")
private val parametersLinePattern = Regex("(?m)^[^:\\n]+:(?=\\s*[a-z][a-z0-9_]*(?::\\[|,|\\s*$))(.*)$")
private val parameterNamePattern = Regex("[a-z][a-z0-9_]*(?=:\\[|,|\\s*$)")

@Composable
internal fun ApiDocumentationSection(rawHtml: String) {
    val contentPrimary = AppTheme.colors.contentPrimary
    val actionColor = AppTheme.colors.settingsTitleAccent
    val nameColor = AppTheme.colors.contentWarning
    val section = remember(rawHtml, contentPrimary, actionColor, nameColor) {
        parseDocumentationSection(rawHtml, contentPrimary, actionColor, nameColor)
    }

    RenderSettingsGroup {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            section.title?.let { title ->
                Text(
                    text = title,
                    style = AppTheme.typography.buttonTitle,
                    color = contentPrimary
                )

                Spacer(Modifier.height(12.dp))

                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(contentPrimary.copy(.08f))
                )

                Spacer(Modifier.height(12.dp))
            }

            SelectionContainer {
                Text(
                    text = section.body,
                    style = AppTheme.typography.cardFormatTitle.copy(lineHeight = 21.sp),
                    color = contentPrimary.copy(.75f)
                )
            }
        }
    }
}

private data class DocumentationSection(val title: String?, val body: AnnotatedString)

private fun parseDocumentationSection(
    rawHtml: String,
    contentPrimary: Color,
    actionColor: Color,
    nameColor: Color
): DocumentationSection {
    val titleMatch = sectionTitlePattern.find(rawHtml)
    val title = titleMatch?.groupValues?.get(1)?.spannedFromHtml()?.toString()?.trim()?.trimEnd(':')
    val bodyHtml = if (titleMatch == null) {
        rawHtml
    } else {
        rawHtml.substring(titleMatch.range.last + 1).replaceFirst(leadingBreaksPattern, "")
    }
    val base = bodyHtml.spannedFromHtml().toAnnotatedString()
    val text = base.text
    val nameStyle = SpanStyle(fontFamily = FontFamily.Monospace, color = nameColor)

    val body = buildAnnotatedString {
        append(base)
        base.spanStyles
            .filter { it.item.fontWeight == FontWeight.Bold }
            .forEach { addStyle(SpanStyle(color = contentPrimary), it.start, it.end) }
        val actionStyle = SpanStyle(fontFamily = FontFamily.Monospace, color = actionColor)
        actionPattern.findAll(text).forEach { addStyle(actionStyle, it.range.first, it.range.last + 1) }
        (quotedNamePattern.findAll(text) + assignedNamePattern.findAll(text)).forEach {
            addStyle(nameStyle, it.range.first, it.range.last + 1)
        }
        parametersLinePattern.findAll(text).forEach { line ->
            val parameters = line.groups[1] ?: return@forEach
            parameterNamePattern.findAll(parameters.value).forEach {
                val start = parameters.range.first + it.range.first
                addStyle(nameStyle, start, start + it.value.length)
            }
        }
    }
    return DocumentationSection(title, body)
}
