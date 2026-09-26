package com.salat.uikit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.uikit.theme.AppTheme

val DialogAppIconShape = RoundedCornerShape(8.dp)

private val AppRowShape = RoundedCornerShape(12.dp)
private val AppRowOuterPadding = 8.dp
private val AppIconSize = 36.dp

@Composable
fun <T> ColumnScope.DialogAppList(
    items: List<T>,
    title: (T) -> String,
    subtitle: (T) -> String,
    isSelected: (T) -> Boolean,
    onClick: (T) -> Unit,
    icon: @Composable (T) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(items, query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            items
        } else {
            items.filter { title(it).contains(trimmed, ignoreCase = true) || subtitle(it).contains(trimmed, true) }
        }
    }

    DialogSearchField(query = query, onQueryChange = { query = it })

    Spacer(Modifier.height(12.dp))

    DialogScrollArea(modifier = Modifier.weight(1f)) {
        if (filtered.isEmpty()) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(DialogTextPadding),
                text = stringResource(R.string.nothing_found),
                style = AppTheme.typography.dialogListTitle,
                color = AppTheme.colors.contentPrimary.copy(SettingsDefaults.SUBTITLE_ALPHA),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = AppRowOuterPadding, vertical = 8.dp)
            ) {
                items(filtered) { item ->
                    DialogAppRow(
                        title = title(item),
                        subtitle = subtitle(item),
                        selected = isSelected(item),
                        onClick = { onClick(item) },
                        icon = { icon(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogAppRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .clip(AppRowShape)
        .background(if (selected) AppTheme.colors.contentAccent.copy(.18f) else Color.Transparent)
        .clickable(onClick = onClick)
        .padding(horizontal = DialogTextPadding - AppRowOuterPadding, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Box(modifier = Modifier.size(AppIconSize), contentAlignment = Alignment.Center) {
        icon()
    }

    Spacer(Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = title,
            style = AppTheme.typography.dialogListTitle,
            color = if (selected) AppTheme.colors.settingsTitleAccent else AppTheme.colors.contentPrimary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Text(
            text = subtitle,
            style = AppTheme.typography.dialogSubtitle,
            color = AppTheme.colors.contentPrimary.copy(.4f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }

    if (selected) {
        Spacer(Modifier.width(12.dp))
        Icon(
            modifier = Modifier.size(22.dp),
            imageVector = Icons.Filled.Check,
            tint = AppTheme.colors.settingsTitleAccent,
            contentDescription = null
        )
    }
}

@Composable
private fun DialogSearchField(query: String, onQueryChange: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val textStyle = AppTheme.typography.dialogListTitle
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DialogContainerPadding)
            .clip(DialogInsetShape)
            .background(AppTheme.colors.surfaceLayer1),
        singleLine = true,
        textStyle = textStyle.copy(color = AppTheme.colors.contentPrimary),
        cursorBrush = SolidColor(AppTheme.colors.contentAccent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .padding(start = 14.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Filled.Search,
                    tint = AppTheme.colors.contentPrimary.copy(SettingsDefaults.SUBTITLE_ALPHA),
                    contentDescription = null
                )

                Spacer(Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_apps),
                            style = textStyle,
                            color = AppTheme.colors.contentPrimary.copy(.4f),
                            maxLines = 1
                        )
                    }
                    innerTextField()
                }

                if (query.isNotEmpty()) {
                    Icon(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onQueryChange("") }
                            .padding(10.dp)
                            .size(20.dp),
                        imageVector = Icons.Filled.Close,
                        tint = AppTheme.colors.contentPrimary.copy(SettingsDefaults.SUBTITLE_ALPHA),
                        contentDescription = null
                    )
                }
            }
        }
    )
}
