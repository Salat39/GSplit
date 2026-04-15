package com.salat.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalContext

@Suppress("MemberVisibilityCanBePrivate")
@Immutable
sealed class UiText {
    @Immutable
    data class DynamicString(val value: CharSequence) : UiText()

    @Immutable
    data class StringResource(
        @StringRes val id: Int,
        val args: List<Any> = emptyList()
    ) : UiText() {
        constructor(@StringRes id: Int, vararg args: Any) : this(id, args.toList())
    }

    @Composable
    fun asCharSequence(): CharSequence = when (this) {
        is DynamicString -> value
        is StringResource -> {
            if (args.isNotEmpty()) {
                LocalContext.current.getString(id, *args.toTypedArray())
            } else {
                LocalContext.current.getString(id)
            }
        }
    }

    @Composable
    fun asString(): String = asCharSequence().toString()

    fun asCharSequence(context: Context): CharSequence = when (this) {
        is DynamicString -> value
        is StringResource -> {
            if (args.isNotEmpty()) {
                context.getString(id, *args.toTypedArray())
            } else {
                context.getString(id)
            }
        }
    }

    fun asString(context: Context): String = asCharSequence(context).toString()

    fun isEmpty(): Boolean = when (this) {
        is DynamicString -> value.isEmpty()
        is StringResource -> id == 0
    }
}
