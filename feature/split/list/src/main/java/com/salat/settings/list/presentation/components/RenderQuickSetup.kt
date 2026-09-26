package com.salat.settings.list.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.common.presentation.components.AdbPortToggler
import com.salat.settings.common.presentation.components.AdbStatusLamp
import com.salat.settings.common.presentation.components.AdbStatusText
import com.salat.settings.common.presentation.entity.DisplayAdbState
import com.salat.settings.list.presentation.ListViewModel
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme

@Composable
internal fun RenderQuickSetup(
    innerPadding: PaddingValues,
    state: ListViewModel.ViewState,
    uiScale: Float?,
    sendAction: (ListViewModel.Action) -> Unit,
    onSkip: () -> Unit
) = BoxWithConstraints(
    modifier = Modifier
        .fillMaxSize()
        .background(AppTheme.colors.surfaceBackground)
        .padding(innerPadding),
    contentAlignment = Alignment.Center
) {
    if (maxWidth > maxHeight) {
        Row(
            modifier = Modifier
                .widthIn(max = 1040.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickSetupHeader(
                horizontalAlignment = Alignment.Start,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AdbConnectionCard(state = state, uiScale = uiScale, sendAction = sendAction)

                Spacer(Modifier.height(12.dp))

                SkipStepButton(onSkip)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuickSetupHeader(horizontalAlignment = Alignment.CenterHorizontally, textAlign = TextAlign.Center)

            Spacer(Modifier.height(28.dp))

            AdbConnectionCard(state = state, uiScale = uiScale, sendAction = sendAction)

            Spacer(Modifier.height(20.dp))

            SkipStepButton(onSkip)
        }
    }
}

@Composable
private fun QuickSetupHeader(
    horizontalAlignment: Alignment.Horizontal,
    textAlign: TextAlign,
    modifier: Modifier = Modifier
) = Column(modifier = modifier, horizontalAlignment = horizontalAlignment) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.contentAccent.copy(.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Build,
            tint = AppTheme.colors.contentAccent,
            modifier = Modifier.size(30.dp),
            contentDescription = null
        )
    }

    Spacer(Modifier.height(20.dp))

    Text(
        text = stringResource(R.string.quick_setup),
        color = AppTheme.colors.contentPrimary,
        style = AppTheme.typography.headline2,
        textAlign = textAlign
    )

    Spacer(Modifier.height(12.dp))

    Text(
        text = stringResource(R.string.quick_setup_desc),
        color = AppTheme.colors.contentPrimary.copy(.7f),
        style = AppTheme.typography.stubTitle,
        textAlign = textAlign
    )
}

@Composable
private fun SkipStepButton(onSkip: () -> Unit) = Text(
    text = stringResource(R.string.skip_step),
    modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .clickable(onClick = onSkip)
        .padding(horizontal = 20.dp, vertical = 12.dp),
    color = AppTheme.colors.contentPrimary.copy(.6f),
    style = AppTheme.typography.buttonTitle
)

@Composable
private fun AdbConnectionCard(
    state: ListViewModel.ViewState,
    uiScale: Float?,
    sendAction: (ListViewModel.Action) -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppTheme.colors.surfaceSettingsLayer1)
            .border(1.dp, AppTheme.colors.contentPrimary.copy(.06f), shape)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AdbStatusLamp(state = state.adbConnectionState, lampSize = 44.dp)

            Spacer(Modifier.width(16.dp))

            AdbStatusText(state = state.adbConnectionState, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.connection_port),
            color = AppTheme.colors.contentPrimary.copy(.7f),
            style = AppTheme.typography.screenTitle
        )

        Spacer(Modifier.height(10.dp))

        AdbPortToggler(
            enableAdbHelper = state.enableAdbHelper,
            adbHelperPort = state.adbHelperPort,
            uiScale = uiScale,
            onSelectPort = { sendAction(ListViewModel.Action.SelectAdbPort(it)) },
            onDisable = { sendAction(ListViewModel.Action.DisableAdb) }
        )
    }
}

@Preview(widthDp = 800, heightDp = 1280)
@Preview(widthDp = 853, heightDp = 320)
@Composable
private fun RenderQuickSetupPreview() {
    PreviewScreen {
        RenderQuickSetup(
            innerPadding = PaddingValues(),
            state = ListViewModel.ViewState(
                adbHelperPort = 5555,
                adbConnectionState = DisplayAdbState.Error("Connection refused")
            ),
            uiScale = 1f,
            sendAction = {},
            onSkip = {}
        )
    }
}
