package com.salat.overlay.presentation

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.salat.overlay.presentation.entity.DisplayPresetType
import com.salat.overlay.presentation.entity.DisplayReplacementAppItem
import com.salat.overlay.presentation.entity.DisplaySplitPreset
import com.salat.overlay.presentation.mappers.toDisplay
import com.salat.overlay.presentation.mappers.toDisplayPreset
import com.salat.preferences.domain.PreferencesRepository
import com.salat.preferences.domain.entity.BoolSharedPref
import com.salat.preferences.domain.entity.FloatSharedPref
import com.salat.preferences.domain.entity.IntSharedPref
import com.salat.replacementappsstorage.domain.repository.ReplacementAppStorageRepository
import com.salat.resources.R
import com.salat.screenspecs.domain.repository.ScreenSpecsRepository
import com.salat.splitpresets.domain.repository.SplitPresetsRepository
import com.salat.statekeeper.domain.entity.AccessibilityServiceEvent
import com.salat.statekeeper.domain.repository.StateKeeperRepository
import com.salat.ui.clickableNoRipple
import com.salat.ui.rememberIsLandscape
import com.salat.ui.rememberPainterResource
import com.salat.uikit.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "overlay_service_channel"
        private const val SECOND_OVERLAY_SHIFT = 220

        private const val REPLACEMENT_MENU_HORIZONTAL_ARRANGEMENT = 16
        private const val REPLACEMENT_MENU_VERTICAL_ARRANGEMENT = 16
        private const val REPLACEMENT_MENU_ICON_TO_TITLE_SPACE = 5
    }

    @Inject
    lateinit var preferences: PreferencesRepository

    @Inject
    lateinit var stateKeeper: StateKeeperRepository

    @Inject
    lateinit var screenSpec: ScreenSpecsRepository

    @Inject
    lateinit var replacementApps: ReplacementAppStorageRepository

    @Inject
    lateinit var splitPresets: SplitPresetsRepository

    private lateinit var windowManager: WindowManager

    // close overlay parameters
    private var interactiveCloseContainer: DraggableFrameLayout? = null
    private var closeWindowParams: WindowManager.LayoutParams? = null
    private var enableCloseOverlay = false
    private var lockCloseOverlay = false
    private var closeOverlayX = -1
    private var closeOverlayY = -1
    private var closeOverlayScale = 1f
    private var closeOverlayBgAlpha = .8f
    private var closeOverlayIconAlpha = .9f

    // replace overlay parameters
    private var interactiveReplaceContainer: DraggableFrameLayout? = null
    private var replaceWindowParams: WindowManager.LayoutParams? = null
    private var enableReplaceOverlay = false
    private var lockReplaceOverlay = false
    private var replaceOverlayX = -1
    private var replaceOverlayY = -1
    private var replaceOverlayScale = 1f
    private var replaceWindowScale = 1f
    private var replaceOverlayBgAlpha = .8f
    private var replaceOverlayIconAlpha = .9f
    private var replaceOverlayAppNames = true
    private var replaceOverlayPresetNames = false

    // menu overlay parameters
    private var interactiveMenuContainer: ComposeView? = null
    private var menuWindowParams: WindowManager.LayoutParams? = null
    private lateinit var composeLifecycleOwner: ComposeWindowLifecycleOwner

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.preset_autorun_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.preset_autorun_channel_desc)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Build and start foreground notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.overlay))
            .setContentText(getString(R.string.overlay_notification_description))
            .setSmallIcon(R.drawable.ic_logo)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
        startForeground(2, notification)
        return START_STICKY
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate() {
        super.onCreate()

        enableCloseOverlay = preferences.getValue(BoolSharedPref.CloseOverlayEnabled)
        enableReplaceOverlay = preferences.getValue(BoolSharedPref.ReplaceOverlayEnabled)

        if (!enableCloseOverlay && !enableReplaceOverlay) return

        // load close overlay prefs
        lockCloseOverlay = preferences.getValue(BoolSharedPref.CloseOverlayLock)
        closeOverlayX = preferences.getValue(IntSharedPref.CloseOverlayX)
        closeOverlayY = preferences.getValue(IntSharedPref.CloseOverlayY)
        closeOverlayScale = preferences.getValue(FloatSharedPref.CloseOverlayScale)
        closeOverlayBgAlpha = preferences.getValue(FloatSharedPref.CloseOverlayBgAlpha)
        closeOverlayIconAlpha = preferences.getValue(FloatSharedPref.CloseOverlayIconAlpha)

        // load replace overlay prefs
        lockReplaceOverlay = preferences.getValue(BoolSharedPref.ReplaceOverlayLock)
        replaceOverlayX = preferences.getValue(IntSharedPref.ReplaceOverlayX)
        replaceOverlayY = preferences.getValue(IntSharedPref.ReplaceOverlayY)
        replaceOverlayScale = preferences.getValue(FloatSharedPref.ReplaceOverlayScale)
        replaceWindowScale = preferences.getValue(FloatSharedPref.ReplaceWindowScale)
        replaceOverlayBgAlpha = preferences.getValue(FloatSharedPref.ReplaceOverlayBgAlpha)
        replaceOverlayIconAlpha = preferences.getValue(FloatSharedPref.ReplaceOverlayIconAlpha)
        replaceOverlayAppNames = preferences.getValue(BoolSharedPref.ReplaceOverlayAppNames)
        replaceOverlayPresetNames = preferences.getValue(BoolSharedPref.ReplaceOverlayPresetsNames)

        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create Compose lifecycle owner
        val myOwner = ComposeWindowLifecycleOwner().apply {
            performRestore(null)
            setCurrentState(Lifecycle.State.RESUMED)
        }
        composeLifecycleOwner = myOwner // Set owner for menu overlay

        // setup close overlay if enabled
        if (enableCloseOverlay) {
            trySetDefaultClosePosition()
            interactiveCloseContainer = DraggableFrameLayout(this, isDragEnabled = !lockCloseOverlay).apply {
                setViewTreeLifecycleOwner(myOwner)
                setViewTreeSavedStateRegistryOwner(myOwner)
                setOnMoveListener { x, y ->
                    closeOverlayX = x
                    closeOverlayY = y
                }
            }
            val closeCompose = ComposeView(this).apply {
                setViewTreeLifecycleOwner(myOwner)
                setViewTreeSavedStateRegistryOwner(myOwner)
                setContent {
                    val density = LocalDensity.current
                    val scaledDensity = remember(density, closeOverlayScale) {
                        Density(
                            density.density * closeOverlayScale,
                            density.fontScale * closeOverlayScale
                        )
                    }
                    val scope = rememberCoroutineScope()
                    fun onClose() {
                        scope.launch {
                            stateKeeper.sendAccessibilityServiceEvent(AccessibilityServiceEvent.CloseSplit)
                        }
                    }
                    AppTheme {
                        CompositionLocalProvider(
                            LocalDensity provides scaledDensity,
                            LocalLayoutDirection provides LayoutDirection.Ltr
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickableNoRipple(onClick = ::onClose)
                                    .background(Color.Black.copy(closeOverlayBgAlpha))
                                    .padding(22.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .alpha(closeOverlayIconAlpha)
                                        .size(26.dp),
                                    // imageVector = Icons.Filled.Close,
                                    painter = rememberPainterResource(R.drawable.ic_close4),
                                    tint = Color.White,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
            interactiveCloseContainer?.addView(closeCompose)
            closeWindowParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = closeOverlayX
                y = closeOverlayY
            }
            interactiveCloseContainer?.wmLayoutParams = closeWindowParams
            windowManager.addView(interactiveCloseContainer, closeWindowParams)
        }

        // setup replace overlay if enabled
        if (enableReplaceOverlay) {
            trySetDefaultReplacePosition()
            interactiveReplaceContainer = DraggableFrameLayout(this, isDragEnabled = !lockReplaceOverlay).apply {
                setViewTreeLifecycleOwner(myOwner)
                setViewTreeSavedStateRegistryOwner(myOwner)
                setOnMoveListener { x, y ->
                    replaceOverlayX = x
                    replaceOverlayY = y
                }
            }
            val replaceCompose = ComposeView(this).apply {
                setViewTreeLifecycleOwner(myOwner)
                setViewTreeSavedStateRegistryOwner(myOwner)
                setContent {
                    val density = LocalDensity.current
                    val scaledDensity = remember(density, replaceOverlayScale) {
                        Density(
                            density.density * replaceOverlayScale,
                            density.fontScale * replaceOverlayScale
                        )
                    }
                    val scope = rememberCoroutineScope()

                    fun onReplace() {
                        scope.launch {
                            val items = replacementApps.getReplacementApps().toDisplay()
                            val presets = splitPresets.getPresets().filter { it.quickAccess }.toDisplayPreset()

                            // show menu overlay
                            showMenuOverlay(items, presets, replaceOverlayAppNames, replaceOverlayPresetNames)
                        }
                    }
                    AppTheme {
                        CompositionLocalProvider(
                            LocalDensity provides scaledDensity,
                            LocalLayoutDirection provides LayoutDirection.Ltr
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickableNoRipple(onClick = ::onReplace)
                                    .background(Color.Black.copy(replaceOverlayBgAlpha))
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .alpha(replaceOverlayIconAlpha)
                                        .scale(scaleX = -1f, scaleY = 1f)
                                        .size(30.dp),
                                    painter = rememberPainterResource(R.drawable.ic_switch7),
                                    tint = Color.White,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
            interactiveReplaceContainer?.addView(replaceCompose)
            replaceWindowParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = replaceOverlayX
                y = replaceOverlayY
            }
            interactiveReplaceContainer?.wmLayoutParams = replaceWindowParams
            windowManager.addView(interactiveReplaceContainer, replaceWindowParams)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // remove close overlay
        interactiveCloseContainer?.let { windowManager.removeView(it) }
        // remove replace overlay
        interactiveReplaceContainer?.let { windowManager.removeView(it) }
        // remove menu overlay
        hideMenuOverlay()

        // save close position
        if (enableCloseOverlay) {
            preferences.setValue(IntSharedPref.CloseOverlayX, closeOverlayX)
            preferences.setValue(IntSharedPref.CloseOverlayY, closeOverlayY)
        }
        // save replace position
        if (enableReplaceOverlay) {
            preferences.setValue(IntSharedPref.ReplaceOverlayX, replaceOverlayX)
            preferences.setValue(IntSharedPref.ReplaceOverlayY, replaceOverlayY)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // set default position for close overlay if not set
    private fun trySetDefaultClosePosition() {
        if (closeOverlayX == -1 || closeOverlayY == -1) {
            val screenH = screenSpec.getFreeScreenHeight()
            val screenW = screenSpec.getFreeScreenWidth()
            closeOverlayX = screenW - (screenW / 6)
            closeOverlayY = screenH / 7
        }
    }

    // set default position for replace overlay if not set
    private fun trySetDefaultReplacePosition() {
        if (replaceOverlayX == -1 || replaceOverlayY == -1) {
            val screenH = screenSpec.getFreeScreenHeight()
            val screenW = screenSpec.getFreeScreenWidth()
            replaceOverlayX = screenW - (screenW / 6)
            replaceOverlayY = (screenH / 7) + SECOND_OVERLAY_SHIFT
        }
    }

    // Show menu overlay centered with items
    @Suppress("DEPRECATION")
    private fun showMenuOverlay(
        items: List<DisplayReplacementAppItem>,
        presets: List<DisplaySplitPreset>,
        appNames: Boolean,
        presetsNames: Boolean
    ) {
        if (interactiveMenuContainer != null) return

        // 1. Calculate margin in pixels (10 dp → px)
        val marginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            10f,
            resources.displayMetrics
        ).toInt()

        // 2. Get screen dimensions
        val displayMetrics = resources.displayMetrics
        val widthPx = displayMetrics.widthPixels - marginPx * 2
        val heightPx = displayMetrics.heightPixels - marginPx * 2

        interactiveMenuContainer = ComposeView(this).apply {
            setViewTreeLifecycleOwner(composeLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(composeLifecycleOwner)
            setContent {
                val density = LocalDensity.current
                val scaledDensity = remember(density, replaceWindowScale) {
                    Density(
                        density.density * replaceWindowScale,
                        density.fontScale * replaceWindowScale
                    )
                }

                val scope = rememberCoroutineScope()
                val isLandscape = rememberIsLandscape()
                val firstTitle = stringResource(if (isLandscape) R.string.left_window else R.string.top_window)
                val secondTitle = stringResource(if (isLandscape) R.string.right_window else R.string.bottom_window)

                AppTheme(darkTheme = true) {
                    CompositionLocalProvider(
                        LocalDensity provides scaledDensity,
                        LocalLayoutDirection provides LayoutDirection.Ltr
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickableNoRipple { hideMenuOverlay() },
                            contentAlignment = Alignment.Center
                        ) {
                            fun itemClick(index: Int, item: DisplayReplacementAppItem) {
                                hideMenuOverlay()
                                scope.launch(Dispatchers.IO) {
                                    stateKeeper.sendAccessibilityServiceEvent(
                                        AccessibilityServiceEvent.ReplaceWindow(
                                            index = index,
                                            packageName = item.packageName,
                                            autoPlay = item.autoPlay
                                        )
                                    )
                                }
                            }

                            fun presetClick(preset: DisplaySplitPreset) {
                                hideMenuOverlay()
                                scope.launch(Dispatchers.IO) {
                                    stateKeeper.sendAccessibilityServiceEvent(
                                        AccessibilityServiceEvent.ReplacePreset(preset.id)
                                    )
                                }
                            }

                            val border = remember { RoundedCornerShape(16.dp) }
                            Box(
                                modifier = Modifier
                                    .padding(26.dp)
                                    .shadow(4.dp, shape = border)
                                    .wrapContentSize()
                                    .background(AppTheme.colors.surfaceBackground.copy(.98f), border)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(rememberScrollState())
                                        .padding(vertical = 16.dp, horizontal = 22.dp)
                                ) {
                                    val firstItems = remember { items.filter { it.firstWindow } }
                                    val secondItems = remember { items.filter { it.secondWindow } }

                                    if (firstItems.isEmpty() && secondItems.isEmpty() && presets.isEmpty()) {
                                        Text(
                                            modifier = Modifier
                                                .padding(24.dp),
                                            text = stringResource(R.string.add_quick_access_apps),
                                            style = AppTheme.typography.confirmDialogTitle,
                                            color = AppTheme.colors.contentPrimary.copy(.6f),
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    if (firstItems.isNotEmpty()) {
                                        MenuAppItem(0, firstTitle, firstItems, appNames, ::itemClick)
                                    }

                                    if (firstItems.isNotEmpty() && secondItems.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    if (secondItems.isNotEmpty()) {
                                        MenuAppItem(1, secondTitle, secondItems, appNames, ::itemClick)
                                    }

                                    if (firstItems.isNotEmpty() || secondItems.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }

                                    if (presets.isNotEmpty()) {
                                        SplitPresetItem(presets, presetsNames, ::presetClick)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Create LayoutParams with the required dimensions and position
        menuWindowParams = WindowManager.LayoutParams(
            widthPx,
            heightPx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            // Position the window from the top-left corner with marginPx offset
            gravity = Gravity.TOP or Gravity.START
            x = marginPx
            y = marginPx
        }

        windowManager.addView(interactiveMenuContainer, menuWindowParams)
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun MenuAppItem(
        index: Int,
        title: String,
        items: List<DisplayReplacementAppItem>,
        appNames: Boolean,
        onClick: (Int, DisplayReplacementAppItem) -> Unit
    ) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            style = AppTheme.typography.confirmDialogTitle,
            color = AppTheme.colors.contentPrimary
        )
        Spacer(Modifier.height(16.dp))
        val extraVerticalSpace = if (appNames) 0 else 8
        FlowRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(REPLACEMENT_MENU_HORIZONTAL_ARRANGEMENT.dp),
            verticalArrangement = Arrangement.spacedBy((REPLACEMENT_MENU_VERTICAL_ARRANGEMENT + extraVerticalSpace).dp)
        ) {
            items.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(64.dp)
                        .clickableNoRipple { onClick(index, item) }
                ) {
                    item.icon?.let {
                        Box {
                            val context = LocalContext.current
                            AsyncImage(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                model = ImageRequest.Builder(context).data(it).build(),
                                contentDescription = item.title,
                                contentScale = ContentScale.Fit
                            )

                            if (item.autoPlay) {
                                Icon(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 2.dp, y = 2.dp)
                                        .alpha(.9f)
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.contentAccent)
                                        .padding(6.dp),
                                    painter =
                                    rememberPainterResource(R.drawable.ic_play),
                                    contentDescription = "",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    if (appNames) {
                        Spacer(Modifier.height(REPLACEMENT_MENU_ICON_TO_TITLE_SPACE.dp))
                        Text(
                            text = item.title,
                            maxLines = 1,
                            style = AppTheme.typography.aboutText,
                            color = AppTheme.colors.contentPrimary,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun SplitPresetItem(
        items: List<DisplaySplitPreset>,
        presetsNames: Boolean,
        onClick: (DisplaySplitPreset) -> Unit
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.presets),
            style = AppTheme.typography.confirmDialogTitle,
            color = AppTheme.colors.contentPrimary
        )
        Spacer(Modifier.height(16.dp))
        FlowRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(REPLACEMENT_MENU_HORIZONTAL_ARRANGEMENT.dp),
            verticalArrangement = Arrangement.spacedBy(REPLACEMENT_MENU_VERTICAL_ARRANGEMENT.dp)
        ) {
            items.forEach { item ->

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(.06f))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .clickableNoRipple { onClick(item) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(48.dp)
                    ) {
                        Box {
                            val context = LocalContext.current
                            AsyncImage(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                model = ImageRequest.Builder(context).data(item.firstApp.icon).build(),
                                contentDescription = item.firstApp.title,
                                contentScale = ContentScale.Fit
                            )

                            if (item.firstApp.autoPlay == true) {
                                Icon(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 2.dp, y = 2.dp)
                                        .alpha(.9f)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.contentAccent)
                                        .padding(5.dp),
                                    painter =
                                    rememberPainterResource(R.drawable.ic_play),
                                    contentDescription = "",
                                    tint = Color.White
                                )
                            }
                        }
                        if (presetsNames) {
                            Spacer(Modifier.height(REPLACEMENT_MENU_ICON_TO_TITLE_SPACE.dp))
                            Text(
                                text = item.firstApp.title,
                                maxLines = 1,
                                style = AppTheme.typography.aboutText,
                                color = AppTheme.colors.contentPrimary,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (item.type) {
                                DisplayPresetType.HALF -> "1x1"
                                DisplayPresetType.ONE_TO_THREE -> "1x2"
                                DisplayPresetType.TWO_TO_THREE -> "2x1"
                                DisplayPresetType.THREE_TO_FOUR -> "3x4"
                                DisplayPresetType.THREE_TO_TWO -> "3x2"
                                DisplayPresetType.FOUR_TO_THREE -> "4x3"
                            },
                            textAlign = TextAlign.Center,
                            style = AppTheme.typography.dialogListTitle,
                            color = AppTheme.colors.contentPrimary
                        )
                        if (item.darkBackground || item.bottomWindowShift) {
                            Spacer(Modifier.height(3.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                if (item.darkBackground) {
                                    Icon(
                                        modifier = Modifier
                                            .size(12.dp),
                                        painter = rememberPainterResource(R.drawable.ic_moon),
                                        contentDescription = null,
                                        tint = AppTheme.colors.contentPrimary
                                    )
                                }

                                if (item.bottomWindowShift) {
                                    Icon(
                                        modifier = Modifier
                                            .size(12.5.dp),
                                        painter = rememberPainterResource(R.drawable.ic_lift),
                                        contentDescription = null,
                                        tint = AppTheme.colors.contentPrimary
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(48.dp)
                    ) {
                        Box {
                            val context = LocalContext.current
                            AsyncImage(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                model = ImageRequest.Builder(context).data(item.secondApp.icon).build(),
                                contentDescription = item.secondApp.title,
                                contentScale = ContentScale.Fit
                            )

                            if (item.secondApp.autoPlay == true) {
                                Icon(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 2.dp, y = 2.dp)
                                        .alpha(.9f)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.contentAccent)
                                        .padding(5.dp),
                                    painter =
                                    rememberPainterResource(R.drawable.ic_play),
                                    contentDescription = "",
                                    tint = Color.White
                                )
                            }
                        }
                        if (presetsNames) {
                            Spacer(Modifier.height(REPLACEMENT_MENU_ICON_TO_TITLE_SPACE.dp))
                            Text(
                                text = item.secondApp.title,
                                maxLines = 1,
                                style = AppTheme.typography.aboutText,
                                color = AppTheme.colors.contentPrimary,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    private fun hideMenuOverlay() {
        interactiveMenuContainer?.let {
            windowManager.removeView(it)
            interactiveMenuContainer = null
        }
    }
}
