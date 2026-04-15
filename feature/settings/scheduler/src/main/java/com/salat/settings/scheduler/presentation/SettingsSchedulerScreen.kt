package com.salat.settings.scheduler.presentation

import android.content.Context
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.salat.resources.R
import com.salat.settings.scheduler.presentation.components.AppScheduleDialog
import com.salat.settings.scheduler.presentation.components.RenderToolbar
import com.salat.ui.rememberPainterResource
import com.salat.uikit.component.ConfirmDialog
import com.salat.uikit.component.TopShadow
import com.salat.uikit.theme.AppTheme

private const val ICON_SIZE = 28

@Composable
internal fun SettingsSchedulerScreen(
    state: SettingsSchedulerViewModel.ViewState,
    sendAction: (SettingsSchedulerViewModel.Action) -> Unit = {},
    uiScaleState: State<Float>? = null,
    onNavigateBack: () -> Unit = {}
) = Scaffold { innerPadding ->

    var showFirstSelectDialog by remember { mutableStateOf(false) }
    if (showFirstSelectDialog) {
        AppScheduleDialog(
            list = state.deviceApps,
            uiScaleState = uiScaleState,
            onDismiss = { showFirstSelectDialog = false },
            onCancel = { showFirstSelectDialog = false },
            onSelect = {
                it?.let { scheduledApp ->
                    sendAction(SettingsSchedulerViewModel.Action.AddScheduledItem(scheduledApp))
                }
            }
        )
    }

    var deleteConfirmDialog by remember { mutableStateOf<Long?>(null) }
    deleteConfirmDialog?.let { id ->
        ConfirmDialog(
            title = stringResource(R.string.deleting_a_preset),
            message = stringResource(R.string.deleting_a_autorun_confirm),
            uiScaleState = uiScaleState,
            negativeAction = true,
            onCancel = { deleteConfirmDialog = null },
            onDismiss = { deleteConfirmDialog = null },
            onClick = {
                sendAction(SettingsSchedulerViewModel.Action.DeleteScheduledItem(id))
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
                    text = stringResource(R.string.scheduler_list_empty),
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
                                            contentDescription = "appIcon",
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    if (app.autoPlay) {
                                        Icon(
                                            modifier = Modifier
                                                .offset(x = 2.dp, y = 2.dp)
                                                .alpha(.9f)
                                                .size(15.dp)
                                                .clip(CircleShape)
                                                .background(AppTheme.colors.contentAccent)
                                                .padding(3.dp),
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

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (app.delay == 0) {
                                            stringResource(R.string.instead_of_split)
                                        } else app.delay.toSecondString(context),
                                        textAlign = TextAlign.Center,
                                        style = if (app.delay == 0) {
                                            AppTheme.typography.cardTitle
                                        } else AppTheme.typography.dialogTitle,
                                        color = AppTheme.colors.contentPrimary
                                    )
                                    if (app.delay != 0) {
                                        Text(
                                            text = stringResource(
                                                if (app.preTask) {
                                                    R.string.before_split
                                                } else R.string.after_split
                                            ).lowercase(),
                                            textAlign = TextAlign.Center,
                                            style = AppTheme.typography.dialogSubtitle,
                                            color = AppTheme.colors.contentPrimary.copy(.7f)
                                        )
                                    }
                                }

                                Spacer(Modifier.width(10.dp))

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

private fun Int.toSecondString(context: Context) = "$this ${context.getString(R.string.sec)}"
