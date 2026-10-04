package com.salat.settings.quicksplit.presentation.components

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
internal fun DrawableImage(drawable: Drawable?, modifier: Modifier = Modifier) {
    drawable?.let {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(it)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    }
}
