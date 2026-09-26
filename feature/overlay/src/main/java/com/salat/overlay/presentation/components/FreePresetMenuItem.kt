package com.salat.overlay.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.salat.overlay.presentation.entity.DisplayAppPreset
import com.salat.overlay.presentation.entity.DisplaySplitPreset
import com.salat.resources.R
import com.salat.ui.clickableNoRipple
import com.salat.ui.rememberPainterResource
import com.salat.ui.systemIconsAreRound
import com.salat.uikit.component.FreeWindowsMap
import com.salat.uikit.theme.AppTheme
import com.salat.uikit.theme.freeWindowColor

private const val CIRCLE_SIZE = 48
private const val CIRCLE_RING = 2
private const val CIRCLE_OVERLAP = 18
private const val CIRCLE_ICON_SIZE = 30
private const val CIRCLE_SQUARE_ICON_SIZE = 26
private const val MAX_CIRCLES = 3
private const val MAP_MAX_WIDTH = 86
private const val BADGE_SIZE = 16
private const val TITLE_SPACE = 5

@Composable
internal fun FreePresetMenuItem(
    item: DisplaySplitPreset,
    presetsNames: Boolean,
    onClick: (DisplaySplitPreset) -> Unit
) = Row(
    modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .background(Color.White.copy(.06f))
        .padding(horizontal = 12.dp, vertical = 10.dp)
        .clickableNoRipple { onClick(item) },
    horizontalArrangement = Arrangement.spacedBy(10.dp)
) {
    Box(Modifier.height(CIRCLE_SIZE.dp), contentAlignment = Alignment.Center) {
        Box {
            val mapWindows = remember(item.windows) { item.windows.map { it.bounds } }
            FreeWindowsMap(mapWindows, maxHeight = CIRCLE_SIZE.dp, maxWidth = MAP_MAX_WIDTH.dp)
            if (item.darkBackground) {
                Badge(
                    iconRes = R.drawable.ic_moon,
                    background = AppTheme.colors.surfaceBackground,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                )
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        WindowsGroup(item)
        if (presetsNames) {
            Spacer(Modifier.height(TITLE_SPACE.dp))
            Text(
                text = pluralStringResource(R.plurals.windows_count, item.windows.size, item.windows.size),
                maxLines = 1,
                style = AppTheme.typography.aboutText,
                color = AppTheme.colors.contentPrimary,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WindowsGroup(item: DisplaySplitPreset) = Box {
    val appCount = if (item.windows.size > MAX_CIRCLES) MAX_CIRCLES - 1 else item.windows.size
    val otherCount = item.windows.size - appCount
    val step = CIRCLE_SIZE - CIRCLE_OVERLAP

    item.windows.take(appCount).forEachIndexed { index, window ->
        Box(Modifier.padding(start = (step * index).dp)) {
            Circle(ringColor = freeWindowColor(index)) {
                AppIcon(window.app)
            }
            if (window.app.autoPlay == true) {
                Badge(
                    iconRes = R.drawable.ic_play,
                    background = AppTheme.colors.contentAccent,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 3.dp, y = 3.dp)
                )
            }
        }
    }

    if (otherCount > 0) {
        Circle(
            ringColor = AppTheme.colors.contentPrimary.copy(.25f),
            modifier = Modifier.padding(start = (step * appCount).dp)
        ) {
            Text(
                text = "+$otherCount",
                style = AppTheme.typography.cardFormatTitle,
                color = AppTheme.colors.contentPrimary
            )
        }
    }
}

@Composable
private fun Circle(ringColor: Color, modifier: Modifier = Modifier, content: @Composable () -> Unit) = Box(
    modifier = modifier.size(CIRCLE_SIZE.dp),
    contentAlignment = Alignment.Center
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(AppTheme.colors.surfaceBackground)
            .border(CIRCLE_RING.dp, ringColor, CircleShape)
    )
    content()
}

@Composable
private fun AppIcon(app: DisplayAppPreset) {
    val context = LocalContext.current
    // A square icon is smaller because its corners come close to the ring
    val size = if (systemIconsAreRound) CIRCLE_ICON_SIZE else CIRCLE_SQUARE_ICON_SIZE
    AsyncImage(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(5.dp)),
        model = remember(app.icon) { ImageRequest.Builder(context).data(app.icon).build() },
        contentDescription = app.title,
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun Badge(iconRes: Int, background: Color, modifier: Modifier) = Icon(
    modifier = modifier
        .alpha(.95f)
        .size(BADGE_SIZE.dp)
        .clip(CircleShape)
        .background(background)
        .padding(3.5.dp),
    painter = rememberPainterResource(iconRes),
    contentDescription = null,
    tint = Color.White
)
