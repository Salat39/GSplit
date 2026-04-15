package com.salat.settings.scheduler.presentation.components

import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun DrawableImage(drawable: Drawable?, modifier: Modifier = Modifier, contentDescription: String? = null) {
    drawable?.let {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(it)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    }
}
