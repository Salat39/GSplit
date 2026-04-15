package com.salat.coil.presentation

import android.content.Context
import coil.Coil
import coil.annotation.ExperimentalCoilApi
import coil.request.CachePolicy
import coil.request.ImageRequest

fun preloadCoilImage(context: Context, imageUrl: String) {
    val imageLoader = Coil.imageLoader(context)

    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .diskCachePolicy(CachePolicy.ENABLED) // Enable disk caching
        .memoryCachePolicy(CachePolicy.ENABLED) // Enable memory caching
        .build()

    // Enqueue the request
    imageLoader.enqueue(request)
}

@OptIn(ExperimentalCoilApi::class)
fun Context.clearCoilCache() {
    try {
        val imageLoader = Coil.imageLoader(this)
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    } catch (_: Exception) {
    }
}
