package com.salat.gsplit.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.salat.gsplit.PresetLauncherActivity
import com.salat.gsplit.presentation.entity.LocalBroadcastEvent
import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.resources.R
import com.salat.schedulerstorage.domain.entity.ScheduledItem
import com.salat.schedulerstorage.domain.repository.SchedulerStorageRepository
import com.salat.splitlauncher.data.repository.SplitLauncherRepositoryImpl.Companion.MURGLAR_PACKAGE
import com.salat.splitlauncher.data.repository.SplitLauncherRepositoryImpl.Companion.VKX_PACKAGE
import com.salat.splitlauncher.data.repository.SplitLauncherRepositoryImpl.Companion.YAM_PACKAGE
import com.salat.splitpresets.domain.repository.SplitPresetsRepository
import com.salat.statekeeper.domain.repository.StateKeeperRepository
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import presentation.openApp
import presentation.sendMurglarAutoPlayCompat
import presentation.sendPlayerAutoPlay
import presentation.sendVkxAutoPlayCompat
import presentation.sendYmAutoPlayCompat
import timber.log.Timber

@AndroidEntryPoint
class WakeUpForegroundService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var actionReceiver: BroadcastReceiver? = null
    private var actionManager: LocalBroadcastManager? = null

    private var ymCompatMode = false
    private var murglarCompatMode = false
    private var vkxCompatMode = false
    private var autoPlayDelay = 3000L

    @Inject
    lateinit var dataStore: DataStoreRepository

    @Inject
    lateinit var splitPresets: SplitPresetsRepository

    @Inject
    lateinit var schedulers: SchedulerStorageRepository

    @Inject
    lateinit var stateKeeperRepository: StateKeeperRepository

    private val client by lazy {
        OkHttpClient.Builder()
            .callTimeout(PING_TIMEOUT, TimeUnit.MILLISECONDS)
            .connectTimeout(PING_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(PING_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(PING_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "wake_up_service_channel"
        private const val PING_URL = "https://clients3.google.com/generate_204"

        private const val CONNECTION_CHECK_LOOP_DELAY = 300L
        private const val PING_TIMEOUT = 1500L
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initActionManager()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Creating notification for foreground service
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.autostart))
            .setContentText(getString(R.string.waiting_for_split_auto_start))
            .setSmallIcon(R.drawable.ic_logo)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        startForeground(1, notification)

        serviceScope.launch {
            val prefData = dataStore.getAnyPrefsFlow(
                BoolPref.SelfAutostart,
                BoolPref.SelfAutostartInBg,
                BoolPref.SelfAutostartByConnect,
                IntPref.AutostartDelay,
                BoolPref.YmCompatPlay,
                BoolPref.MurglarCompatPlay,
                BoolPref.VkxCompatPlay,
                IntPref.AutoPlayDelay
            ).firstOrNull() ?: return@launch

            val autostart = prefData[0] as Boolean
            val autostartInBg = prefData[1] as Boolean
            val autostartByConnect = prefData[2] as Boolean
            val autostartDelay = (prefData[3] as Int).toLong()
            ymCompatMode = prefData[4] as Boolean
            murglarCompatMode = prefData[5] as Boolean
            vkxCompatMode = prefData[6] as Boolean
            autoPlayDelay = (prefData[7] as Int).toLong()

            if (!autostart) {
                stopSelf()
                return@launch
            }
            val extraLaunchQueue = schedulers.getSchedulers()

            if (autostartByConnect) {
                initNetworkHandler(this@WakeUpForegroundService) {
                    doBootJob(autostartInBg, autostartDelay, extraLaunchQueue)
                    Timber.d("[CONNECTIVITY MANAGER] Internet confirmed")
                }
            } else {
                doBootJob(autostartInBg, autostartDelay, extraLaunchQueue)
            }
        }

        return START_STICKY
    }

    private suspend fun doBootJob(autostartInBg: Boolean, autostartDelay: Long, extraLaunchQueue: List<ScheduledItem>) {
        val (preTasks, afterTasks) = extraLaunchQueue.partition { it.preTask }
        val overrideMainLaunch = extraLaunchQueue.any { it.delay == 0 }

        if (autostartDelay > 0) {
            Timber.d("Delay before autostart: $autostartDelay")
            delay(autostartDelay)
        }

        // Launch all pre tasks in parallel and wait for their completion
        runExtraPreTasks(preTasks)

        if (!overrideMainLaunch) {
            if (!autostartInBg) {
                launchApp()
            } else splitPresets.getPresets().find { it.autoStart }?.let { preset ->
                // Skip next app launch split event
                stateKeeperRepository.setSkipAutoLaunch(true)
                launchPreset(preset.id)
            }
        } else {
            // Skip next app launch split event
            stateKeeperRepository.setSkipAutoLaunch(true)
        }

        // Launch all after tasks in parallel and wait for their completion
        runExtraPostTasks(afterTasks)

        stopSelf()
    }

    private suspend fun runExtraPostTasks(tasks: List<ScheduledItem>) {
        if (tasks.isNotEmpty()) {
            var autoPlayTask: Deferred<Unit>? = null
            coroutineScope {
                // Launch each task in its own coroutine
                val jobs = tasks.map { scheduledItem ->
                    launch {
                        // Wait for the specified time (if delay is in seconds, multiply by 1000)
                        delay(scheduledItem.delay * 1000L)

                        // wait auto play task if exist
                        autoPlayTask?.await()

                        // auto play task
                        if (scheduledItem.autoPlay) {
                            autoPlayTask?.cancel()
                            autoPlayTask = async { autoPlay(scheduledItem.packageName) }
                        }

                        // Launch the app by packageName
                        withContext(Dispatchers.Main) {
                            openApp(scheduledItem.packageName)
                        }
                        Timber.d("[SCHEDULER] launch post task: ${scheduledItem.packageName}")
                    }
                }
                // Wait for all launched coroutines to complete
                jobs.joinAll()

                // wait auto play task if exist
                autoPlayTask?.await()
                autoPlayTask = null
            }
        }
    }

    private suspend fun runExtraPreTasks(tasks: List<ScheduledItem>) {
        if (tasks.isNotEmpty()) {
            // The maximum delay value determines when the main code should start
            val maxDelay = tasks.maxOf { it.delay }
            var autoPlayTask: Deferred<Unit>? = null
            coroutineScope {
                // For each task, calculate how long to wait so it launches at the right moment
                tasks.forEach { scheduledItem ->
                    launch {
                        // If the task's delay is less than the maximum, wait for the difference
                        val waitTime = (maxDelay - scheduledItem.delay) * 1000L
                        if (waitTime > 0) {
                            delay(waitTime)
                        }

                        // wait auto play task if exist
                        autoPlayTask?.await()

                        // auto play task
                        if (scheduledItem.autoPlay) {
                            autoPlayTask?.cancel()
                            autoPlayTask = async { autoPlay(scheduledItem.packageName) }
                        }

                        // Launch the app, which should start scheduledItem.delay seconds before the main code
                        withContext(Dispatchers.Main) {
                            openApp(scheduledItem.packageName)
                        }
                        Timber.d("[SCHEDULER] launch pre task: ${scheduledItem.packageName}")
                    }
                }
                // Wait until the moment the main code should start, i.e., the maximum time from all timers
                delay(maxDelay * 1000L)

                // wait auto play task if exist
                autoPlayTask?.await()
                autoPlayTask = null
            }
        }
    }

    private suspend fun autoPlay(packageName: String) {
        delay(autoPlayDelay + 100L)

        try {
            if (ymCompatMode && packageName == YAM_PACKAGE) {
                withContext(Dispatchers.Main) { sendYmAutoPlayCompat() }
                return
            }

            withContext(Dispatchers.Main) { sendPlayerAutoPlay(packageName) }

            if (murglarCompatMode && packageName == MURGLAR_PACKAGE) {
                delay(300L)
                withContext(Dispatchers.Main) { sendMurglarAutoPlayCompat() }
            }

            if (vkxCompatMode && packageName == VKX_PACKAGE) {
                delay(300L)
                withContext(Dispatchers.Main) { sendVkxAutoPlayCompat() }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private suspend fun launchApp() = withContext(Dispatchers.Main) {
        val uiLaunch = Intent(this@WakeUpForegroundService, MainActivity::class.java)
        uiLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(uiLaunch)
    }

    private suspend fun launchPreset(id: Long) = withContext(Dispatchers.Main) {
        val noUiLaunch = Intent(this@WakeUpForegroundService, PresetLauncherActivity::class.java)
        noUiLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        noUiLaunch.putExtra("id", id)
        startActivity(noUiLaunch)
    }

    /**
     * Tracks the global status of device's Internet connection
     */
    private fun CoroutineScope.initNetworkHandler(context: Context, onConnected: suspend () -> Unit) = launch {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            private val availableNetworks: MutableSet<Network> = HashSet()
            private var internetCheckJob: Job? = null

            // Flag to ensure onConnected is called only once
            private var hasCalledConnected = false

            override fun onAvailable(network: Network) {
                availableNetworks.add(network)
                startChecking(network, cm, onConnected)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                    startChecking(network, cm, onConnected)
                }
            }

            override fun onLost(network: Network) {
                availableNetworks.remove(network)
                if (availableNetworks.isEmpty()) {
                    internetCheckJob?.cancel()
                    internetCheckJob = null
                }
            }

            private fun startChecking(network: Network, cm: ConnectivityManager, onConnected: suspend () -> Unit) {
                // If onConnected has already been called or a check is already running for this network, do nothing
                if (hasCalledConnected || (internetCheckJob?.isActive == true)) return

                // Start a recurring check with a short delay (300 ms)
                internetCheckJob = serviceScope.launch {
                    while (isActive) {
                        val capabilities = cm.getNetworkCapabilities(network)
                        if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true &&
                            isInternetAvailable()
                        ) {
                            hasCalledConnected = true
                            onConnected()
                            break
                        }
                        delay(CONNECTION_CHECK_LOOP_DELAY) // Small delay to minimize load
                    }
                }
            }
        }

        cm.registerNetworkCallback(networkRequest, networkCallback!!)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun isInternetAvailable(): Boolean = withTimeoutOrNull(PING_TIMEOUT) {
        suspendCancellableCoroutine { cont ->
            val request = Request.Builder()
                .url(PING_URL)
                .get()
                .build()
            val call = client.newCall(request)
            cont.invokeOnCancellation { call.cancel() }
            call.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    if (!cont.isCompleted) cont.resume(false) {}
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (!cont.isCompleted) cont.resume(response.code == 204) {}
                }
            })
        }
    } ?: false

    // Method to create Notification Channel (required for Android 8.0+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.preset_autorun_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.preset_autorun_channel_desc)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun initActionManager() {
        actionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent != null && intent.action != null) {
                    // Any actions here
                    when (intent.action) {
                        LocalBroadcastEvent.APP_LAUNCHED -> {
                            stopSelf()
                        }
                    }
                }
            }
        }

        actionManager = LocalBroadcastManager.getInstance(this)
        val ar = IntentFilter().apply {
            addAction(LocalBroadcastEvent.APP_LAUNCHED)
        }
        actionReceiver?.let { actionManager?.registerReceiver(it, ar) }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.let { callback ->
            try {
                val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                cm.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        actionReceiver?.let { actionManager?.unregisterReceiver(it) }
        serviceScope.cancel()
    }
}
