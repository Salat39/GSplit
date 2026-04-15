package com.salat.settings.api.presentation

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderGroupTitle
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.TopShadow
import com.salat.uikit.component.toAnnotatedString
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme
import presentation.spannedFromHtml

@Composable
internal fun SettingsApiScreen(
    state: SettingsApiViewModel.ViewState,
    sendAction: (SettingsApiViewModel.Action) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
            .padding(innerPadding)
    ) {
        RenderToolbar("API", onNavigateBack)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surfaceSettingsLayer1)
                ) {
                    RenderGroupTitle(stringResource(R.string.synchronization))

                    RenderSwitcher(
                        title = stringResource(R.string.send_events_to_macrodroid),
                        subtitle = stringResource(R.string.send_events_to_macrodroid_desc),
                        value = state.macroDroidEventSync,
                        groupDivider = false,
                        onChange = { sendAction(SettingsApiViewModel.Action.SetMacroDroidEventSync(it)) }
                    )
                }

                RenderGroupDivider()

                SelectionContainer {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.api_text)
                                .spannedFromHtml()
                                .toAnnotatedString(),
                            style = AppTheme.typography.aboutText,
                            color = AppTheme.colors.contentPrimary.copy(.5f)
                        )

                        Spacer(Modifier.height(20.dp))
                        Spacer(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(.2f))
                        )
                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = stringResource(R.string.api_text3)
                                .spannedFromHtml()
                                .toAnnotatedString(),
                            style = AppTheme.typography.aboutText,
                            color = AppTheme.colors.contentPrimary.copy(.5f)
                        )

                        Spacer(Modifier.height(20.dp))
                        Spacer(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(.2f))
                        )
                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = stringResource(R.string.api_text2)
                                .spannedFromHtml()
                                .toAnnotatedString(),
                            style = AppTheme.typography.aboutText,
                            color = AppTheme.colors.contentPrimary.copy(.5f)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Preview
@Composable
private fun SettingsApiScreenPreview() {
    PreviewScreen {
        SettingsApiScreen(
            state = SettingsApiViewModel.ViewState()
        )
    }
}
