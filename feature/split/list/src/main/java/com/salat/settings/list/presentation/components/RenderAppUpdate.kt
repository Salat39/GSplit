package com.salat.settings.list.presentation.components

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.salat.resources.R
import com.salat.settings.list.presentation.entity.DisplayAppUpdate
import com.salat.settings.list.presentation.entity.UiDownloadState
import com.salat.ui.rememberPainterResource
import com.salat.uikit.component.ThinWhiteProgress
import com.salat.uikit.component.toAnnotatedString
import com.salat.uikit.theme.AppTheme
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import presentation.spannedFromHtml

private val UpdateCardShape = RoundedCornerShape(20.dp)
private val UpdateIconTileSize = 40.dp
private val UpdateIconTextGap = 12.dp

@Composable
internal fun RenderAppUpdate(
    info: DisplayAppUpdate,
    updateDownloadState: UiDownloadState?,
    onStartDownload: (String) -> Unit,
    onSkip: () -> Unit
) {
    Spacer(Modifier.height(8.dp))

    val context = LocalContext.current
    val targetColor = if (info.mandatory) {
        AppTheme.colors.deleteButton
    } else {
        AppTheme.colors.warning
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(UpdateCardShape)
            .background(AppTheme.colors.surfaceSettingsLayer1)
            .border(1.dp, targetColor.copy(.45f), UpdateCardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(UpdateIconTileSize)
                    .clip(RoundedCornerShape(12.dp))
                    .background(targetColor.copy(.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = rememberPainterResource(R.drawable.ic_import),
                    contentDescription = null,
                    tint = lerp(targetColor, Color.White, .45f),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(UpdateIconTextGap))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.new_version_available),
                    color = AppTheme.colors.contentPrimary,
                    style = AppTheme.typography.cardTitle
                )
                if (info.version.isNotEmpty()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = info.version,
                        color = AppTheme.colors.contentPrimary.copy(.5f),
                        style = AppTheme.typography.dialogSubtitle
                    )
                }
            }
            if (!info.mandatory) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.skip),
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onSkip)
                        .wrapContentHeight()
                        .padding(horizontal = 12.dp),
                    color = AppTheme.colors.contentPrimary.copy(.5f),
                    style = AppTheme.typography.cardFormatTitle,
                    maxLines = 1
                )
            }
        }

        if (info.text.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(start = UpdateIconTileSize + UpdateIconTextGap),
                text = info.text
                    .spannedFromHtml()
                    .toAnnotatedString(),
                color = AppTheme.colors.contentPrimary.copy(.7f),
                style = AppTheme.typography.aboutText
            )
        }

        if (info.downloadUrl.isNotEmpty() || info.infoUrl.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = UpdateIconTileSize + UpdateIconTextGap),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (updateDownloadState is UiDownloadState.InProgress) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                        modifier = Modifier.weight(1f)
                    ) {
                        val percent = updateDownloadState.percent
                        ThinWhiteProgress(
                            percent = percent,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (info.size.isNotEmpty()) {
                            Text(
                                text = "${info.size.scaleSize(percent)} / ${info.size}",
                                color = AppTheme.colors.contentPrimary,
                                style = AppTheme.typography.dialogSubtitle
                            )
                        }
                    }
                }

                if (info.infoUrl.isNotEmpty()) {
                    UpdateButton(
                        text = stringResource(R.string.details),
                        background = AppTheme.colors.contentPrimary.copy(.08f),
                        onClick = { context.openUrlSmart(info.infoUrl) }
                    )
                }

                if (info.downloadUrl.isNotEmpty()) {
                    UpdateButton(
                        text = stringResource(R.string.download),
                        background = AppTheme.colors.contentAccent,
                        enabled = updateDownloadState == null,
                        onClick = { onStartDownload(info.downloadUrl) }
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(8.dp))
}

@Composable
private fun UpdateButton(text: String, background: Color, enabled: Boolean = true, onClick: () -> Unit) = Text(
    text = text,
    modifier = Modifier
        .heightIn(min = 40.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(if (enabled) background else AppTheme.colors.contentPrimary.copy(.05f))
        .clickable(enabled = enabled, onClick = onClick)
        .wrapContentHeight()
        .padding(horizontal = 18.dp, vertical = 8.dp),
    color = AppTheme.colors.contentPrimary.copy(if (enabled) 1f else .38f),
    style = AppTheme.typography.cardFormatTitle,
    maxLines = 1
)

private fun String.scaleSize(percent: Int): String {
    // Split strictly by space; collapse multiple spaces first.
    val parts = trim().split(" ").filter { it.isNotEmpty() }
    if (parts.size != 2) return this

    val numberPart = parts[0].replace(',', '.') // allow "1,5"
    val unitPart = parts[1]

    val value = numberPart.toBigDecimalOrNull() ?: return this

    // Scale: value * percent / 100, keep precision before final rounding
    val scaled = value.multiply(BigDecimal(percent))
        .divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
        .setScale(1, RoundingMode.HALF_UP) // exactly one decimal place

    // Always dot as decimal separator, one decimal place
    val df = DecimalFormat("#0.0").apply {
        decimalFormatSymbols = decimalFormatSymbols.apply { decimalSeparator = '.' }
    }

    return "${df.format(scaled)} $unitPart"
}

/** Try to open a web link reliably with graceful fallbacks */
private fun Context.openUrlSmart(rawUrl: String, chooserTitle: String = "Open link"): OpenUrlResult {
    val normalized = rawUrl.trim().let { s ->
        if (s.startsWith("http://") || s.startsWith("https://")) s else "https://$s"
    }

    val uri: Uri =
        runCatching { normalized.toUri() }.getOrNull() ?: return OpenUrlResult.INVALID_URL

    // Build a standard VIEW intent constrained to browsers/browsable apps
    val viewIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        addCategory(Intent.CATEGORY_BROWSABLE) // hint: web-capable handlers only
        if (this@openUrlSmart !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val pm = packageManager

    // If there is at least one handler for VIEW http/https, let the user choose one.
    //    (We pre-check to avoid the "black screen" no-handler flash.)
    val viewHandlers = pm.queryIntentActivities(viewIntent, PackageManager.MATCH_DEFAULT_ONLY)
    if (viewHandlers.isNotEmpty()) {
        val chooser = Intent.createChooser(viewIntent, chooserTitle).apply {
            if (this@openUrlSmart !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(chooser) }
            .onFailure { /* swallow ActivityNotFound/Security just in case */ }
        return OpenUrlResult.OPENED_IN_APP
    }

    // No browser? Offer to share the link anywhere (messenger, email, etc.).
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, normalized)
        if (this@openUrlSmart !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val shareHandlers = pm.queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY)
    if (shareHandlers.isNotEmpty()) {
        val chooser = Intent.createChooser(shareIntent, "Share link").apply {
            if (this@openUrlSmart !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(chooser) }
        return OpenUrlResult.SHARED
    }

    // There's nothing to open and nowhere to look — let's copy it to the clipboard.
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("URL", normalized))
    Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show() // UI hint
    return OpenUrlResult.COPIED_TO_CLIPBOARD
}

private enum class OpenUrlResult {
    OPENED_IN_APP, // started an external app via ACTION_VIEW
    SHARED, // showed system share sheet with the link
    COPIED_TO_CLIPBOARD, // copied when no handlers found at all
    INVALID_URL // couldn't even parse the URL
}
