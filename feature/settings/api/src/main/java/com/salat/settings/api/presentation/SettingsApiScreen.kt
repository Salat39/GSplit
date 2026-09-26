package com.salat.settings.api.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.api.presentation.components.ApiDocumentationSection
import com.salat.settings.common.presentation.RenderGroupDivider
import com.salat.settings.common.presentation.RenderGroupTitle
import com.salat.settings.common.presentation.RenderSettingsContent
import com.salat.settings.common.presentation.RenderSettingsGroup
import com.salat.settings.common.presentation.RenderToolbar
import com.salat.uikit.component.RenderSwitcher
import com.salat.uikit.component.TopShadow
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme

private val apiDocumentationSections = listOf(R.string.api_text, R.string.api_text3, R.string.api_text2)

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

            RenderSettingsContent {
                RenderSettingsGroup {
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

                Spacer(Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    apiDocumentationSections.forEach { sectionRes ->
                        ApiDocumentationSection(stringResource(sectionRes))
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
