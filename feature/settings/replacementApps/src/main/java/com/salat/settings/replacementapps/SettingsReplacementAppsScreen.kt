package com.salat.settings.replacementapps

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.salat.resources.R
import com.salat.settings.replacementapps.components.AddReplacementAppDialog
import com.salat.settings.replacementapps.components.RenderToolbar
import com.salat.settings.replacementapps.entity.DisplayReplacementAppItem
import com.salat.ui.rememberIsLandscape
import com.salat.ui.rememberPainterResource
import com.salat.uikit.component.ConfirmDialog
import com.salat.uikit.component.TopShadow
import com.salat.uikit.preview.PreviewScreen
import com.salat.uikit.theme.AppTheme

private const val ICON_SIZE = 28

@Composable
internal fun SettingsReplacementAppsScreen(
    state: SettingsReplacementAppsViewModel.ViewState,
    sendAction: (SettingsReplacementAppsViewModel.Action) -> Unit = {},
    uiScaleState: State<Float>? = null,
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->

    var showFirstSelectDialog by remember { mutableStateOf(false) }
    if (showFirstSelectDialog) {
        AddReplacementAppDialog(
            list = state.deviceApps,
            uiScaleState = uiScaleState,
            onDismiss = { showFirstSelectDialog = false },
            onCancel = { showFirstSelectDialog = false },
            onSelect = {
                it?.let { data ->
                    sendAction(
                        SettingsReplacementAppsViewModel.Action.AddReplacementAppItem(
                            app = data.app,
                            firstWindow = data.first,
                            secondWindow = data.second,
                            autoPlay = data.autoPlay
                        )
                    )
                }
            }
        )
    }

    var deleteConfirmDialog by remember { mutableStateOf<Long?>(null) }
    deleteConfirmDialog?.let { id ->
        ConfirmDialog(
            title = stringResource(R.string.deleting_a_preset),
            message = stringResource(R.string.deleting_a_quick_app_confirm),
            uiScaleState = uiScaleState,
            negativeAction = true,
            onCancel = { deleteConfirmDialog = null },
            onDismiss = { deleteConfirmDialog = null },
            onClick = {
                sendAction(SettingsReplacementAppsViewModel.Action.DeleteReplacementAppItem(id))
                deleteConfirmDialog = null
            }
        )
    }

    fun onDeleteDialog(id: Long) {
        deleteConfirmDialog = id
    }

    val isEmpty by remember(state.dataLoaded, state.items) {
        derivedStateOf {
            state.dataLoaded && state.items.isEmpty()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceBackground)
            .padding(innerPadding)
    ) {
        RenderToolbar(
            onAddClick = { showFirstSelectDialog = true },
            onBack = onNavigateBack
        )

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(AppTheme.colors.surfaceLayer1)
        ) {
            TopShadow()

            if (isEmpty) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .align(Alignment.Center),
                    text = stringResource(R.string.add_quick_access_app),
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
                item(key = -1) {
                    Spacer(
                        Modifier
                            .height(8.dp)
                    )
                }
                itemsIndexed(
                    items = state.items,
                    key = { _, item -> item.id }
                ) { _, app ->
                    val animatedModifier = remember {
                        Modifier.animateItem(
                            fadeInSpec = itemFadeAnimationSpec,
                            fadeOutSpec = itemFadeAnimationSpec,
                            placementSpec = itemSlideAnimationSpec
                        )
                    }

                    val context = LocalContext.current
//                    var clickLock by rememberTimeLockedBoolean(1000L)

                    Card(
                        modifier = animatedModifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardItemBackground)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppTheme.colors.cardItemBackground)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(ICON_SIZE.dp),
                                    contentAlignment = Alignment.BottomEnd
                                ) {
                                    app.icon?.let {
                                        AsyncImage(
                                            modifier = Modifier
                                                .size(ICON_SIZE.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            model = remember(app.packageName) {
                                                ImageRequest.Builder(context)
                                                    .data(it)
                                                    .build()
                                            },
                                            contentDescription = "firstAppIcon",
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    if (app.autoPlay) {
                                        Icon(
                                            modifier = Modifier
                                                .offset(x = 3.dp, y = 3.dp)
                                                .alpha(.9f)
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(AppTheme.colors.contentAccent)
                                                .padding(3.5.dp),
                                            painter = rememberPainterResource(R.drawable.ic_play),
                                            contentDescription = "",
                                            tint = Color.White
                                        )
                                    }
                                }

                                Spacer(Modifier.width(10.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = app.title,
                                        style = AppTheme.typography.cardTitle,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        color = if (app.autoPlay) {
                                            AppTheme.colors.contentLightAccent
                                        } else AppTheme.colors.contentPrimary
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = AppTheme.typography.dialogSubtitle,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        color = AppTheme.colors.contentPrimary.copy(.5f)
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.available_in) + ":",
                                        textAlign = TextAlign.Center,
                                        style = AppTheme.typography.dialogSubtitle,
                                        color = AppTheme.colors.contentPrimary
                                    )

                                    Spacer(Modifier.width(12.dp))

                                    val isLandscape = rememberIsLandscape()
                                    if (isLandscape) {
                                        Row(
                                            modifier = Modifier,
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(36.dp)
                                                    .height(50.dp)
                                                    .clip(
                                                        RoundedCornerShape(
                                                            topStart = 6.dp,
                                                            bottomStart = 6.dp
                                                        )
                                                    )
                                                    .background(
                                                        if (app.firstWindow) {
                                                            AppTheme.colors.addSplitTop
                                                        } else AppTheme.colors.surfaceMenu
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (app.firstWindow) "1" else "X",
                                                    textAlign = TextAlign.Center,
                                                    style = if (app.firstWindow) {
                                                        AppTheme.typography.dialogSubtitle
                                                    } else AppTheme.typography.dialogSubtitle.copy(fontSize = 9.sp),
                                                    color = AppTheme.colors.contentPrimary
                                                )
                                            }

                                            Spacer(Modifier.width(4.dp))

                                            Box(
                                                modifier = Modifier
                                                    .width(36.dp)
                                                    .height(50.dp)
                                                    .clip(
                                                        RoundedCornerShape(
                                                            topEnd = 6.dp,
                                                            bottomEnd = 6.dp
                                                        )
                                                    )
                                                    .background(
                                                        if (app.secondWindow) {
                                                            AppTheme.colors.addSplitTop
                                                        } else AppTheme.colors.surfaceMenu
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    if (app.secondWindow) "2" else "X",
                                                    textAlign = TextAlign.Center,
                                                    style = if (app.secondWindow) {
                                                        AppTheme.typography.dialogSubtitle
                                                    } else AppTheme.typography.dialogSubtitle.copy(fontSize = 9.sp),
                                                    color = AppTheme.colors.contentPrimary
                                                )
                                            }
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier,
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(50.dp)
                                                    .height(22.dp)
                                                    .clip(
                                                        RoundedCornerShape(
                                                            topStart = 6.dp,
                                                            topEnd = 6.dp
                                                        )
                                                    )
                                                    .background(
                                                        if (app.firstWindow) {
                                                            AppTheme.colors.addSplitTop
                                                        } else AppTheme.colors.surfaceMenu
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (app.firstWindow) "1" else "X",
                                                    textAlign = TextAlign.Center,
                                                    style = if (app.firstWindow) {
                                                        AppTheme.typography.dialogSubtitle
                                                    } else AppTheme.typography.dialogSubtitle.copy(fontSize = 9.sp),
                                                    color = AppTheme.colors.contentPrimary
                                                )
                                            }

                                            Spacer(Modifier.height(4.dp))

                                            Box(
                                                modifier = Modifier
                                                    .width(50.dp)
                                                    .height(22.dp)
                                                    .clip(
                                                        RoundedCornerShape(
                                                            bottomStart = 6.dp,
                                                            bottomEnd = 6.dp
                                                        )
                                                    )
                                                    .background(
                                                        if (app.secondWindow) {
                                                            AppTheme.colors.addSplitTop
                                                        } else AppTheme.colors.surfaceMenu
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    if (app.secondWindow) "2" else "X",
                                                    textAlign = TextAlign.Center,
                                                    style = if (app.secondWindow) {
                                                        AppTheme.typography.dialogSubtitle
                                                    } else AppTheme.typography.dialogSubtitle.copy(fontSize = 9.sp),
                                                    color = AppTheme.colors.contentPrimary
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.width(16.dp))

                                IconButton(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(start = 2.dp),
                                    onClick = { onDeleteDialog(app.id) }
                                ) {
                                    Icon(
                                        modifier = Modifier.size(22.dp),
                                        imageVector = Icons.Filled.Delete,
                                        tint = AppTheme.colors.deleteButton,
                                        contentDescription = "delete"
                                    )
                                }
                            }
                        }
                    }
                }
                item(key = -2) {
                    Spacer(Modifier.height(36.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun SettingsReplacementAppsScreenPreview() {
    PreviewScreen {
        SettingsReplacementAppsScreen(
            state = SettingsReplacementAppsViewModel.ViewState(
                items = listOf(
                    DisplayReplacementAppItem(
                        title = "test1",
                        packageName = "pckg",
                        firstWindow = true,
                        secondWindow = true,
                        autoPlay = true,
                        icon = null,
                        id = 1
                    ),
                    DisplayReplacementAppItem(
                        title = "test2",
                        packageName = "pckg",
                        firstWindow = true,
                        secondWindow = false,
                        autoPlay = false,
                        icon = null,
                        id = 2
                    ),
                    DisplayReplacementAppItem(
                        title = "test2",
                        packageName = "pckg",
                        firstWindow = false,
                        secondWindow = true,
                        autoPlay = true,
                        icon = null,
                        id = 3
                    ),
                )
            )
        )
    }
}
