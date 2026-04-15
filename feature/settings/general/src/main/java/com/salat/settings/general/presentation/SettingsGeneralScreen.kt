package com.salat.settings.general.presentation

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.salat.resources.R
import com.salat.settings.common.presentation.RenderGroupButton
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderGroupTitle
import com.salat.settings.common.presentation.RenderIconMenuDivider
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.ui.rememberIsLandscape
import com.salat.uikit.component.ClickableUnderlinedText
import com.salat.uikit.component.TopShadow
import com.salat.uikit.component.toAnnotatedString
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme
import presentation.openAppSystemSettings
import presentation.sendEmail
import presentation.spannedFromHtml

private const val EMAIL = "svc.andrei@gmail.com"
private const val TG = "salat39"

@Composable
internal fun SettingsGeneralScreen(
    state: SettingsGeneralViewModel.ViewState,
    sendAction: (SettingsGeneralViewModel.Action) -> Unit = {},
    onNavigateToAutostart: () -> Unit = {},
    onNavigateToPresets: () -> Unit = {},
    onNavigateToUi: () -> Unit = {},
    onNavigateToAdb: () -> Unit = {},
    onNavigateToClosingOverlay: () -> Unit = {},
    onNavigateToAppSwitchOverlay: () -> Unit = {},
    onNavigateToDarkScreenMode: () -> Unit = {},
    onNavigateToWindowShiftMode: () -> Unit = {},
    onNavigateToAppTasks: () -> Unit = {},
    onNavigateToApi: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
            .padding(innerPadding)
    ) {
        RenderToolbar(stringResource(R.string.settings), onNavigateBack)
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppTheme.colors.surfaceSettings)
        ) {
            TopShadow()

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // General group
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderGroupTitle(stringResource(R.string.general))

                    // autostart
                    RenderGroupButton(
                        title = stringResource(R.string.autostart),
                        subtitle = stringResource(R.string.auto_start_presets),
                        icon = painterResource(R.drawable.ic_launch),
                        iconSize = 29,
                        onClick = onNavigateToAutostart
                    )

                    RenderIconMenuDivider()

                    // launching presets
                    RenderGroupButton(
                        title = stringResource(R.string.launching_presets),
                        subtitle = stringResource(R.string.freeform_window_settings),
                        icon = painterResource(R.drawable.ic_split_launch),
                        iconSize = 27,
                        onClick = onNavigateToPresets
                    )

                    RenderIconMenuDivider()

                    // ui
                    RenderGroupButton(
                        title = stringResource(R.string.ui),
                        subtitle = stringResource(R.string.interface_display_settings),
                        icon = painterResource(R.drawable.ic_ui5),
                        iconSize = 29,
                        onClick = onNavigateToUi
                    )

                    RenderIconMenuDivider()

                    // adb
                    RenderGroupButton(
                        title = stringResource(R.string.adb_features),
                        subtitle = stringResource(R.string.adb_features_description),
                        icon = painterResource(R.drawable.ic_android),
                        iconSize = 29,
                        onClick = onNavigateToAdb
                    )
                }

                RenderGroupDivider()

                Spacer(Modifier.height(12.dp))

                // Modes
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderGroupTitle(stringResource(R.string.modes))

                    // dark background
                    RenderGroupButton(
                        title = stringResource(R.string.dark_background),
                        subtitle = stringResource(R.string.dark_background_split),
                        icon = painterResource(R.drawable.ic_moon),
                        onClick = onNavigateToDarkScreenMode
                    )

                    val isLandscape = rememberIsLandscape()
                    if (!isLandscape) {
                        RenderIconMenuDivider()

                        // window shift
                        RenderGroupButton(
                            title = stringResource(R.string.window_shift),
                            subtitle = stringResource(R.string.hide_lower_system_bar),
                            icon = painterResource(R.drawable.ic_shift_up),
                            onClick = onNavigateToWindowShiftMode
                        )
                    }
                }

                RenderGroupDivider()

                Spacer(Modifier.height(12.dp))

                // Overlay
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderGroupTitle(stringResource(R.string.overlay))

                    // Split close
                    RenderGroupButton(
                        title = stringResource(R.string.split_close),
                        subtitle = stringResource(R.string.split_close_description),
                        icon = rememberVectorPainter(image = Icons.Filled.Close),
                        iconSize = 28,
                        onClick = onNavigateToClosingOverlay
                    )

                    RenderIconMenuDivider()

                    // Window switch
                    RenderGroupButton(
                        title = stringResource(R.string.app_switch),
                        subtitle = stringResource(R.string.app_switch_description),
                        icon = painterResource(R.drawable.ic_window_replace),
                        iconSize = 27,
                        onClick = onNavigateToAppSwitchOverlay
                    )
                }

                RenderGroupDivider()

                Spacer(Modifier.height(12.dp))

                // Other
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderGroupTitle(stringResource(R.string.other))

                    RenderGroupButton(
                        title = stringResource(R.string.launch_with_tasks),
                        subtitle = stringResource(R.string.send_additional_actions),
                        icon = painterResource(R.drawable.ic_app_task2),
                        iconSize = 29,
                        onClick = onNavigateToAppTasks
                    )

                    RenderIconMenuDivider()

                    RenderGroupButton(
                        title = "API",
                        subtitle = stringResource(R.string.manage_presets_and_windows),
                        icon = painterResource(R.drawable.ic_api),
                        iconSize = 27,
                        onClick = onNavigateToApi
                    )

                    RenderIconMenuDivider()

                    RenderGroupButton(
                        title = buildString {
                            append(stringResource(R.string.app_label))
                            append(" ")
                            append(stringResource(R.string.settings).lowercase())
                        },
                        subtitle = stringResource(R.string.quick_android_settings),
                        iconSize = 26,
                        onClick = {
                            context.openAppSystemSettings(context.applicationInfo.packageName)
                        }
                    )
                }

                RenderGroupDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    SelectionContainer {
                        ClickableUnderlinedText(
                            text = stringResource(R.string.about_text, EMAIL, TG)
                                .spannedFromHtml()
                                .toAnnotatedString(),
                            style = AppTheme.typography.aboutText,
                            color = AppTheme.colors.contentPrimary.copy(.4f),
                            underlineColor = AppTheme.colors.contentAccent
                        ) { url ->

                            when (url) {
                                "#email" -> context.sendEmail(EMAIL)

                                "#tg" -> {
                                    val uri = "tg://resolve?domain=$TG".toUri()
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.telegram_not_installed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                else -> Unit
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Preview
@Composable
private fun SettingsGeneralScreenPreview() {
    PreviewScreen {
        SettingsGeneralScreen(
            state = SettingsGeneralViewModel.ViewState()
        )
    }
}
