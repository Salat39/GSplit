package com.salat.settings.list.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.salat.resources.R
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.settings.list.presentation.entity.RenderListType
import com.salat.ui.rememberIsLandscape
import com.salat.uikit.component.AnimatedPopup
import com.salat.uikit.component.BottomShadow
import com.salat.uikit.component.OptionsMenuItem
import com.salat.uikit.component.TopShadow
import com.salat.uikit.theme.AppTheme
import presentation.openAppSystemSettings

@Composable
internal fun ListMenu(
    itemState: MutableState<Pair<RenderListType, DisplaySplitPreset>?>,
    positionOffset: MutableState<IntOffset>,
    onDelete: (Long) -> Unit,
    onEdit: (id: Long, type: Int) -> Unit,
    onSetAuthStart: (id: Long, enable: Boolean) -> Unit,
    onSetDarkBackground: (id: Long, enable: Boolean) -> Unit,
    onSetWindowShift: (id: Long, enable: Boolean) -> Unit,
    onSetQuickAccess: (id: Long, enable: Boolean) -> Unit,
    uiScaleState: State<Float>?
) {
    itemState.value?.let { (type, item) ->
        fun onClose() {
            itemState.value = null
        }

        val isLandscape = rememberIsLandscape()

        BackHandler(onBack = ::onClose)

        Box {
            AnimatedPopup(
                offset = positionOffset.value,
                uiScaleState = uiScaleState,
                properties = PopupProperties(
                    focusable = true,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                ),
                onDismissRequest = ::onClose
            ) {
                Column {
                    val border = remember { RoundedCornerShape(6.dp) }
                    // Shadow wrapper
                    Box(
                        Modifier
                            .padding(vertical = 5.dp, horizontal = 8.dp)
                            .shadow(4.dp, shape = border)
                            .padding(1.dp)
                            .width(IntrinsicSize.Max)
                            .height(IntrinsicSize.Max)
                    ) {
                        // Menu items list
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .background(AppTheme.colors.surfaceMenu, border)
                        ) {
                            val context = LocalContext.current
                            OptionsMenuItem(
                                R.drawable.ic_open_window,
                                buildString {
                                    append(item.firstApp.title)
                                    append(" ")
                                    append(stringResource(R.string.settings).lowercase())
                                }
                            ) {
                                onClose()
                                context.openAppSystemSettings(item.firstApp.packageName)
                            }

                            OptionsMenuItem(
                                R.drawable.ic_open_window,
                                buildString {
                                    append(item.secondApp.title)
                                    append(" ")
                                    append(stringResource(R.string.settings).lowercase())
                                }
                            ) {
                                onClose()
                                context.openAppSystemSettings(item.secondApp.packageName)
                            }

                            if (type == RenderListType.PRESET) {
                                Box(
                                    modifier = Modifier
                                        .background(AppTheme.colors.surfaceMenuDivider)
                                        .height(16.dp)
                                ) {
                                    TopShadow()

                                    BottomShadow(modifier = Modifier.align(Alignment.BottomCenter))
                                }

                                if (item.autoStart) {
                                    OptionsMenuItem(R.drawable.ic_cancel, R.string.disable_autorun) {
                                        onClose()
                                        onSetAuthStart(item.id, false)
                                    }
                                } else {
                                    OptionsMenuItem(R.drawable.ic_autorun, R.string.enable_autorun) {
                                        onClose()
                                        onSetAuthStart(item.id, true)
                                    }
                                }
                                if (item.darkBackground) {
                                    OptionsMenuItem(
                                        R.drawable.ic_moon_off,
                                        R.string.disable_dark_background,
                                        scale = .9f
                                    ) {
                                        onClose()
                                        onSetDarkBackground(item.id, false)
                                    }
                                } else {
                                    OptionsMenuItem(R.drawable.ic_moon, R.string.enable_dark_background, scale = .9f) {
                                        onClose()
                                        onSetDarkBackground(item.id, true)
                                    }
                                }
                                if (!isLandscape) {
                                    if (item.bottomWindowShift) {
                                        OptionsMenuItem(R.drawable.ic_shift_down, R.string.disable_shift, scale = .9f) {
                                            onClose()
                                            onSetWindowShift(item.id, false)
                                        }
                                    } else {
                                        OptionsMenuItem(R.drawable.ic_shift_up, R.string.enable_shift, scale = .9f) {
                                            onClose()
                                            onSetWindowShift(item.id, true)
                                        }
                                    }
                                }
                                if (item.quickAccess) {
                                    OptionsMenuItem(
                                        R.drawable.ic_star_fall,
                                        R.string.disable_quick_access,
                                        scale = .9f
                                    ) {
                                        onClose()
                                        onSetQuickAccess(item.id, false)
                                    }
                                } else {
                                    OptionsMenuItem(R.drawable.ic_star, R.string.enable_quick_access, scale = .9f) {
                                        onClose()
                                        onSetQuickAccess(item.id, true)
                                    }
                                }

                                OptionsMenuItem(R.drawable.ic_edit, R.string.edit, scale = .9f) {
                                    onClose()
                                    onEdit(item.id, item.type.id)
                                }
                                OptionsMenuItem(R.drawable.ic_delete, R.string.delete) {
                                    onClose()
                                    onDelete(item.id)
                                }
                            }
                        }
                    }

                    // ID title
                    if (type == RenderListType.PRESET) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(horizontal = 10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AppTheme.colors.surfaceMenu.copy(.85f))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                            text = "ID: ${item.id}",
                            style = AppTheme.typography.idTitle,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = AppTheme.colors.contentPrimary.copy(.5f)
                        )
                    } else {
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
