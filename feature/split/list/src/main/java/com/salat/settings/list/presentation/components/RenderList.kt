package com.salat.settings.list.presentation.components

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.salat.resources.R
import com.salat.settings.list.presentation.ListViewModel
import com.salat.settings.list.presentation.entity.DisplaySplitPreset
import com.salat.settings.list.presentation.entity.RenderListType
import com.salat.settings.list.presentation.entity.UiDownloadState
import com.salat.ui.toPxInt
import com.salat.uikit.component.ConfirmDialog
import com.salat.uikit.component.TopShadow
import com.salat.uikit.theme.AppTheme
import presentation.toast
import presentation.vibrate

@Composable
internal fun RenderList(
    state: ListViewModel.ViewState,
    sendAction: (ListViewModel.Action) -> Unit = {},
    uiScaleState: State<Float>?,
    toolbarExtraSizeState: State<Int>?,
    onNavigateToAdd: (editId: Long?, type: Int?) -> Unit = { _, _ -> },
    onNavigateToSettings: () -> Unit = {},
    innerPadding: PaddingValues
) = Column(
    modifier = Modifier
        .fillMaxSize()
        .background(AppTheme.colors.surfaceBackground)
        .padding(innerPadding)
) {
    val context = LocalContext.current

    val menuItem = remember { mutableStateOf<Pair<RenderListType, DisplaySplitPreset>?>(null) }
    val yShift = remember { 52.dp.toPxInt }
    val xShift = remember { 64.dp.toPxInt }
    val menuOffset = remember { mutableStateOf(IntOffset.Zero) }
    fun onOpenMenu(item: DisplaySplitPreset, type: RenderListType, offset: Offset) {
        menuItem.value = type to item
        menuOffset.value = IntOffset(
            x = offset.x.toInt() - xShift,
            y = offset.y.toInt() - yShift
        )
        context.vibrate()
    }

    var deleteConfirmDialog by remember { mutableStateOf<Long?>(null) }
    deleteConfirmDialog?.let { id ->
        ConfirmDialog(
            title = stringResource(R.string.deleting_a_preset),
            message = stringResource(R.string.deleting_a_preset_confirm),
            uiScaleState = uiScaleState,
            negativeAction = true,
            onCancel = { deleteConfirmDialog = null },
            onDismiss = { deleteConfirmDialog = null },
            onClick = {
                sendAction(ListViewModel.Action.DeletePreset(id))
                deleteConfirmDialog = null
            }
        )
    }

    fun onDeleteDialog(id: Long) {
        deleteConfirmDialog = id
    }

    ListMenu(
        itemState = menuItem,
        uiScaleState = uiScaleState,
        positionOffset = menuOffset,
        onDelete = { onDeleteDialog(it) },
        onEdit = { id, type -> onNavigateToAdd(id, type) },
        onSetAuthStart = { id, enable -> sendAction(ListViewModel.Action.MarkAutoStartupPreset(id, enable)) },
        onSetDarkBackground = { id, enable -> sendAction(ListViewModel.Action.MarkDarkBackgroundPreset(id, enable)) },
        onSetWindowShift = { id, enable -> sendAction(ListViewModel.Action.MarkWindowShiftPreset(id, enable)) },
        onSetQuickAccess = { id, enable -> sendAction(ListViewModel.Action.MarkQuickAccessPreset(id, enable)) },
    )

    val isEmptyPresets by remember(state.dataLoaded, state.items) {
        derivedStateOf {
            state.dataLoaded && state.items.isEmpty()
        }
    }
    val isEmptyHistory by remember(state.showLastLaunchedSplit, state.history) {
        derivedStateOf {
            (state.showLastLaunchedSplit && state.history == null) || !state.showLastLaunchedSplit
        }
    }

    // Toolbar extra space
    val density = LocalDensity.current
    val toolbarHeight = remember(toolbarExtraSizeState?.value) {
        with(density) { toolbarExtraSizeState?.value?.toDp() ?: 0.dp }
    }
    Spacer(Modifier.height(toolbarHeight))

    RenderToolbar(
        onAddClick = { onNavigateToAdd(null, null) },
        onSettingsClick = onNavigateToSettings
    )
    Box(
        Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(AppTheme.colors.surfaceLayer1)
    ) {
        TopShadow()

        if (isEmptyPresets && isEmptyHistory) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .align(Alignment.Center),
                text = stringResource(R.string.main_list_empty),
                style = AppTheme.typography.dialogListTitle,
                color = AppTheme.colors.contentPrimary.copy(.6f),
                textAlign = TextAlign.Center
            )
        }

        val itemFadeAnimationSpec = remember { spring<Float>(stiffness = 1700f) } // 25_000f test
        val itemSlideAnimationSpec = remember { spring<IntOffset>(stiffness = 1700f) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            item(key = -2) {
                Spacer(
                    Modifier
                        .height(8.dp)
                )
            }

            state.appUpdateInfo?.let { info ->
                item(key = -4) {
                    RenderAppUpdate(
                        info = info,
                        updateDownloadState = state.updateDownloadState,
                        onStartDownload = { url -> sendAction(ListViewModel.Action.StartDownloadUpdate(url)) }
                    )

                    LaunchedEffect(state.updateDownloadState) {
                        runCatching {
                            if (state.updateDownloadState is UiDownloadState.Error) {
                                context.toast(context.getString(R.string.data_fetch_failed))
                                sendAction(ListViewModel.Action.SetDownloadUpdateState(null))
                            }

                            if (state.updateDownloadState is UiDownloadState.Success) {
                                val uri = state.updateDownloadState.uri
                                    .toContentUri(context)
                                promptInstall(context, uri)
                                sendAction(ListViewModel.Action.SetDownloadUpdateState(null))
                            }
                        }
                    }
                }
            }

            if (state.showLastLaunchedSplit) {
                state.history?.let { history ->
                    item(key = -1) {
                        val animatedModifier = remember {
                            Modifier.animateItem(
                                fadeInSpec = itemFadeAnimationSpec,
                                fadeOutSpec = itemFadeAnimationSpec,
                                placementSpec = itemSlideAnimationSpec
                            )
                        }
                        RenderListItem(
                            animatedModifier,
                            history,
                            if (state.lastLaunchedSplitContrast) {
                                RenderListType.HISTORY_CONTRAST
                            } else RenderListType.HISTORY,
                            onClick = {
                                sendAction(ListViewModel.Action.PrepareOpenSplit(history))
                            },
                            onLongClick = { item, offset ->
                                onOpenMenu(item, RenderListType.HISTORY, offset)
                            }
                        )
                    }
                }
            }
            itemsIndexed(
                items = state.items,
                key = { _, item -> item.id }
            ) { _, preset ->
                val animatedModifier = remember {
                    Modifier.animateItem(
                        fadeInSpec = itemFadeAnimationSpec,
                        fadeOutSpec = itemFadeAnimationSpec,
                        placementSpec = itemSlideAnimationSpec
                    )
                }
                RenderListItem(animatedModifier, preset, RenderListType.PRESET, onClick = {
                    sendAction(ListViewModel.Action.PrepareOpenSplit(preset))
                }, onLongClick = { item, offset ->
                    onOpenMenu(item, RenderListType.PRESET, offset)
                })
            }
            item(key = -3) {
                Spacer(
                    Modifier
                        .height(36.dp)
                )
            }
        }
    }
}
