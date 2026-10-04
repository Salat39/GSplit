package com.salat.adb.data.repository

import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.util.Base64
import com.salat.adb.BuildConfig
import com.salat.adb.data.entity.AdbConnectionState
import com.salat.adb.data.entity.AdbRecentTaskInfo
import com.salat.adb.data.entity.LegacyStack
import com.salat.adb.data.entity.SHIZUKU_HELPER_PORT
import com.salat.adb.data.entity.TELNET_HELPER_PORT
import com.salat.adb.domain.repository.AdbRepository
import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.PreferencesRepository
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.IntPref
import com.salat.preferences.domain.entity.PrivateStringSharedPref
import com.tananaev.adblib.AdbBase64
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import com.tananaev.adblib.AdbStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku
import timber.log.Timber

class AdbRepositoryImpl(
    private val dataStore: DataStoreRepository,
    private val preferences: PreferencesRepository
) : AdbRepository {

    companion object {
        private const val TIMEOUT_MS = 5_000
        private const val AUTH_TIMEOUT_MS = 20_000L
        private const val RECONNECT_DELAY_MS = 3_000L
        private const val MAX_RECONNECT_RETRIES = 5
        private const val CONNECT_AWAIT_MS = 6_000L
        private const val NEW_TASK_LOOKUP_ATTEMPTS = 15
        private const val NEW_TASK_LOOKUP_INTERVAL_SEC = "0.05"
        private const val MOVED_TO_WINDOW = "MOVED_TO_WINDOW"

        // A removed task stays in the dump for a short time. The record of its finishing activity has the f mark
        private const val SKIP_FINISHING_ACTIVITIES = "grep -v ' f}'"

        private const val DONE_PREFIX = "__ADB_DONE__:"
        private const val SHIZUKU_PERMISSION_REQUEST_CODE = 668
    }

    private val host
        get() = if (BuildConfig.DEBUG) "10.0.2.2" else "localhost"

    // Manages IO scope for background tasks.
    private val ioScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    private val lock = Mutex()
    private val reconnectMutex = Mutex()

    private val connGuard = Any()

    @Volatile
    private var reconnectJob: Job? = null

    @Volatile
    private var isManuallyDisconnected: Boolean = false

    @Volatile
    private var connectionEpoch: Long = 0L

    private val base64 = AdbBase64 { data -> Base64.encodeToString(data, Base64.NO_WRAP) }

    private var savedCrypto: AdbCrypto? = null
    private val telnetDiscovery by lazy { TelnetShellDiscovery(::buildDoneMarker) }

    private val _connectionState =
        MutableStateFlow<AdbConnectionState>(AdbConnectionState.Disconnected)
    override val connectionState: StateFlow<AdbConnectionState> = _connectionState.asStateFlow()

    @Volatile
    private var socket: Socket? = null

    @Volatile
    private var connection: AdbConnection? = null

    @Volatile
    private var telnetTransport: TelnetShellTransport? = null

    @Volatile
    private var shizukuTransport: ShizukuShellTransport? = null

    @Volatile
    private var shizukuPermissionRequested = false

    private val taskIdRegex = Regex(
        pattern = """\bTask\{[^}]*#(\d+)\b""",
        options = setOf(RegexOption.MULTILINE)
    )

    private val windowModeRegex = Regex(""" mode=(freeform|multi-window) """)

    private val taskWindowingModeRegex = Regex(""" mode=([\w-]+) """)

    // The pattern does not match a finishing root activity
    private val rootActivityRegex = Regex("""Hist +#0: ActivityRecord\{[0-9a-f]+ u\d+ ([\w.]+)/[^ }]+\}? t(\d+)\}""")

    private val launchedFromPackageRegex = Regex("""\blaunchedFromPackage=(\S+)""")

    private val removedTaskRegex = Regex("""\bREMOVED=(\d+)""")

    private val resizeModeRegex = Regex("""\bresizeMode=(\w+)""")
    private val resizeableModes = setOf("RESIZE_MODE_RESIZEABLE", "RESIZE_MODE_RESIZEABLE_VIA_SDK_VERSION")

    private val foregroundPackageRegex = Regex(
        pattern = """\b([a-zA-Z0-9_]+(?:\.[a-zA-Z0-9_]+)+)(?=(?:/|\s|\}|,|\)|\]|$))"""
    )

    // Recents tasks
    private val taskHeaderRegex = Regex("""^\s{2}\*\sTask\{.*#(\d+).*?\btype=([a-zA-Z_]+)\b.*""")
    private val taskHeaderVisibleRegex = Regex("""\bvisible=(true|false)\b""")
    private val taskHeaderVisibleRequestedRegex = Regex("""\bvisibleRequested=(true|false)\b""")
    private val taskHeaderPackageFromARegex =
        Regex("""\bA=\d+:([a-zA-Z0-9_]+(?:\.[a-zA-Z0-9_]+)+)\b""")
    private val packageNameLineRegex =
        Regex("""\bpackageName=([a-zA-Z0-9_]+(?:\.[a-zA-Z0-9_]+)+)\b""")
    private val activityStateRegex = Regex("""\bstate=([A-Z_]+)\b""")
    private val nowVisibleRegex = Regex("""\bnowVisible=(true|false)\b""")
    private val lastVisibleTimeRegex = Regex("""\blastVisibleTime=([^\s]+)\b""")
    private val baseDirRegex = Regex("""\bbaseDir=([^\s]+)\b""")
    private val dataDirRegex = Regex("""\bdataDir=([^\s]+)\b""")

    init {
        // Drop connection when external toggle becomes OFF.
        ioScope.launch {
            // Disconnect by disable
            launch {
                dataStore.getBooleanPrefFlow(BoolPref.EnableAdbHelper).collect { enable ->
                    if (!enable) disconnect() else reconnect()
                }
            }
            // Disconnect by port changed
            launch {
                dataStore.getIntPrefFlow(IntPref.AdbHelperPort).drop(1).collect { _ ->
                    disconnect()
                    val enable =
                        dataStore.getBooleanPrefFlow(BoolPref.EnableAdbHelper).first()
                    if (enable) reconnect()
                }
            }
        }

        Shizuku.addBinderReceivedListenerSticky { reconnectShizukuIfSelected() }
        Shizuku.addBinderDeadListener {
            ioScope.launch { dropDeadShizukuTransport() }
        }
        Shizuku.addRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE && grantResult == PackageManager.PERMISSION_GRANTED) {
                reconnectShizukuIfSelected()
            }
        }
    }

    /**
     * Connects to adbd at host:port with ephemeral RSA keys; idempotent.
     */
    suspend fun connect(host: String, port: Int) = when {
        isTelnetMode(port) -> connectTelnet()
        isShizukuMode(port) -> connectShizuku()
        else -> connectAdb(host, port)
    }

    private suspend fun connectAdb(host: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        lock.withLock {
            // Snapshot epoch to prevent resurrecting connection after disconnect().
            val myEpoch = synchronized(connGuard) { connectionEpoch }

            isManuallyDisconnected = false

            if (isAdbConnectedUnsafe()) {
                _connectionState.value = AdbConnectionState.Connected
                Timber.d("[ADB] connect skipped: already connected")
                cancelReconnectLoop()
                return@withLock true
            }

            _connectionState.value = AdbConnectionState.Connecting
            Timber.d("[ADB] connect to %s:%d", host, port)
            try {
                val s = Socket()
                s.connect(InetSocketAddress(host, port), TIMEOUT_MS)

                val conn = AdbConnection.create(s, adbCrypto())
                conn.connectAuthorized(s)

                // Do not publish connection if disconnect() happened during connect().
                val canPublish = synchronized(connGuard) {
                    connectionEpoch == myEpoch && !isManuallyDisconnected
                }
                if (!canPublish) {
                    runCatching { conn.close() }
                    runCatching { s.close() }
                    _connectionState.value = AdbConnectionState.Disconnected
                    return@withLock false
                }

                synchronized(connGuard) {
                    // Re-check inside the critical section to avoid races.
                    if (connectionEpoch != myEpoch || isManuallyDisconnected) {
                        runCatching { conn.close() }
                        runCatching { s.close() }
                        _connectionState.value = AdbConnectionState.Disconnected
                        return@withLock false
                    }
                    val oldTelnet = telnetTransport
                    socket = s
                    connection = conn
                    telnetTransport = null
                    runCatching { oldTelnet?.close() }
                }

                _connectionState.value = AdbConnectionState.Connected
                Timber.d("[ADB] connected to %s:%d", host, port)
                cancelReconnectLoop()
                true
            } catch (t: Throwable) {
                _connectionState.value = AdbConnectionState.Error(t.message ?: "ADB connect error")
                Timber.w(t, "[ADB] connect error")
                safeClose()
                scheduleReconnect(host, port, "connect error")
                false
            }
        }
    }

    // A saved key lets the user allow the app once. A storage error leaves a new key for each connection
    private fun adbCrypto(): AdbCrypto {
        savedCrypto?.let { return it }
        loadSavedCrypto()?.let { crypto ->
            savedCrypto = crypto
            return crypto
        }
        val keyPair = runCatching {
            KeyPairGenerator.getInstance("RSA").apply { initialize(AdbCrypto.KEY_LENGTH_BITS) }.generateKeyPair()
        }.onFailure { Timber.e(it) }.getOrNull() ?: return AdbCrypto.generateAdbKeyPair(base64)
        val crypto = AdbCrypto.loadAdbKeyPair(base64, keyPair)
        if (saveKeyPair(keyPair)) savedCrypto = crypto
        return crypto
    }

    private fun loadSavedCrypto(): AdbCrypto? = runCatching {
        val privateKey = preferences.getValue(PrivateStringSharedPref.AdbPrivateKey) ?: return null
        val publicKey = preferences.getValue(PrivateStringSharedPref.AdbPublicKey) ?: return null
        if (preferences.getValue(PrivateStringSharedPref.AdbKeyFingerprint) != keyFingerprint(privateKey, publicKey)) {
            Timber.w("[ADB] saved key fingerprint mismatch")
            return null
        }
        val keyFactory = KeyFactory.getInstance("RSA")
        val keyPair = KeyPair(
            keyFactory.generatePublic(X509EncodedKeySpec(Base64.decode(publicKey, Base64.NO_WRAP))),
            keyFactory.generatePrivate(PKCS8EncodedKeySpec(Base64.decode(privateKey, Base64.NO_WRAP)))
        )
        AdbCrypto.loadAdbKeyPair(base64, keyPair)
    }.onFailure { Timber.e(it) }.getOrNull()

    private fun saveKeyPair(keyPair: KeyPair): Boolean = runCatching {
        val privateKey = Base64.encodeToString(keyPair.private.encoded, Base64.NO_WRAP)
        val publicKey = Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP)
        preferences.setValues(
            mapOf(
                PrivateStringSharedPref.AdbPrivateKey to privateKey,
                PrivateStringSharedPref.AdbPublicKey to publicKey,
                PrivateStringSharedPref.AdbKeyFingerprint to keyFingerprint(privateKey, publicKey)
            )
        )
    }.onFailure { Timber.e(it) }.getOrDefault(false)

    private fun keyFingerprint(privateKey: String, publicKey: String) = MessageDigest.getInstance("SHA-256")
        .digest("$privateKey:$publicKey".toByteArray())
        .joinToString("") { "%02x".format(it) }

    // Android 10 does not finish the connection after the user allows a new key. The saved key passes next time
    private fun AdbConnection.connectAuthorized(socket: Socket) {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.Q) return connect()
        if (connect(AUTH_TIMEOUT_MS, TimeUnit.MILLISECONDS, false)) return
        runCatching { close() }
        runCatching { socket.close() }
        throw IOException("ADB authorization timeout")
    }

    /**
     * Ensures background reconnect is attempted when enabled.
     */
    private suspend fun reconnect() {
        isManuallyDisconnected = false

        if (_connectionState.value is AdbConnectionState.Disconnected) {
            // Keep state machine simple: reconnect() is the only entry that can leave Disconnected.
            _connectionState.value = AdbConnectionState.Connecting
        }

        val port = dataStore.getIntPrefFlow(IntPref.AdbHelperPort).first()
        if (!isConnectedForPortUnsafe(port)) {
            val ok = connect(host, port)
            if (!ok) {
                scheduleReconnect(host, port, "initial reconnect")
            }
        }
    }

    /**
     * Starts a single reconnect loop with fixed delay and capped retries.
     */
    private suspend fun scheduleReconnect(host: String, port: Int, reason: String) {
        // If the user explicitly disconnected, do not auto-reconnect and reset retry budget.
        if (isManuallyDisconnected || _connectionState.value is AdbConnectionState.Disconnected) {
            Timber.d(
                "[ADB] reconnect suppressed: manual disconnect/state disconnected (%s)",
                reason
            )
            cancelReconnectLoop()
            return
        }

        reconnectMutex.withLock {
            if (reconnectJob?.isActive == true) return

            Timber.w("[ADB] scheduling reconnect: %s", reason)
            reconnectJob = ioScope.launch {
                var attempt = 0
                while (isActive && attempt < MAX_RECONNECT_RETRIES) {
                    if (isManuallyDisconnected || _connectionState.value is AdbConnectionState.Disconnected) {
                        // Disconnect must reset attempts and stop the loop.
                        break
                    }
                    val enable = dataStore.getBooleanPrefFlow(BoolPref.EnableAdbHelper).first()
                    if (!enable) {
                        disconnect()
                        break
                    }
                    if (isConnectedForPortUnsafe(port)) {
                        break
                    }

                    attempt++
                    Timber.d(
                        "[ADB] reconnect attempt %d/%d in %dms",
                        attempt,
                        MAX_RECONNECT_RETRIES,
                        RECONNECT_DELAY_MS,
                    )
                    delay(RECONNECT_DELAY_MS)
                    val ok = connect(host, port)
                    if (ok) {
                        Timber.d("[ADB] reconnected")
                        break
                    }
                }

                reconnectMutex.withLock {
                    reconnectJob = null
                }
            }
        }
    }

    /**
     * Executes "shell:<command>" with lazy connect and background reconnect on failure.
     */
    override suspend fun execute(command: String): String = withContext(Dispatchers.IO) {
        val enable = dataStore.getBooleanPrefFlow(BoolPref.EnableAdbHelper).first()
        if (!enable) {
            disconnect()
            return@withContext "ADB helper disabled"
        }

        // If disconnected intentionally, do not attempt any background reconnects.
        if (isManuallyDisconnected || _connectionState.value is AdbConnectionState.Disconnected) {
            return@withContext "ADB disconnected"
        }

        val port = dataStore.getIntPrefFlow(IntPref.AdbHelperPort).first()

        if (!isConnectedForPortUnsafe(port)) {
            val ok = connect(host, port)
            if (!ok) return@withContext "ADB connect failed"
        }

        if (command.isEmpty()) return@withContext "empty command"

        try {
            when {
                isTelnetMode(port) -> executeTelnetWithReconnect(command)
                isShizukuMode(port) -> executeShizukuLocked(command)
                else -> executeLocked(command)
            }
        } catch (t: CommandFailedException) {
            // Command-level failure: connection is alive (marker reached), no reconnect required.
            Timber.w(
                t,
                "[ADB] command failed (exit=%d), output=%s",
                t.exitCode,
                t.output.trim().takeLast(300)
            )
            t.message ?: "ADB command failed"
        } catch (t: Throwable) {
            // If disconnected intentionally, do not attempt any background reconnects.
            if (isManuallyDisconnected || _connectionState.value is AdbConnectionState.Disconnected) {
                return@withContext "ADB disconnected"
            }

            Timber.w(t, "[ADB] execute failed")
            dropConnectionForRetry(t)
            scheduleReconnect(host, port, "execute error")
            t.message ?: "ADB execute error"
        }
    }

    override suspend fun isAppInWindow(packageName: String): Boolean? {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || pkg.equals("unknown", ignoreCase = true) || !isValidPackageName(pkg)) {
            return false
        }

        val taskId = getTaskId(pkg) ?: return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return findLegacyStack(taskId)?.mode == "freeform"
        val headers = execute("dumpsys activity activities | grep -E '\\* Task\\{[0-9a-f]+ #$taskId '")
        return " mode=freeform " in headers || " mode=multi-window " in headers
    }

    override suspend fun isAppLaunched(packageName: String): Boolean {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || pkg.equals("unknown", ignoreCase = true) || !isValidPackageName(pkg)) {
            return false
        }

        val result = execute("pidof $pkg 2>/dev/null || true").trim()
        if (result.isEmpty()) return false

        return result.split(' ', '\n', '\r', '\t').any { pid ->
            pid.toIntOrNull()?.let { it > 0 } == true
        }
    }

    override suspend fun hasForegroundService(packageName: String): Boolean {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || pkg.equals("unknown", ignoreCase = true) || !isValidPackageName(pkg)) {
            return false
        }

        val componentPrefix = "$pkg/"
        // The dump starts with the last ANR service of any package. The services of the package follow it
        return execute("dumpsys activity services $componentPrefix")
            .substringAfter("active services:", "")
            .contains("isForeground=true")
    }

    override suspend fun getTaskId(packageName: String): Int? {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || pkg.equals("unknown", ignoreCase = true) || !isValidPackageName(pkg)) {
            return null
        }
        val rootActivities = execute("dumpsys activity activities | grep -E 'Hist +#0:' | $SKIP_FINISHING_ACTIVITIES")
        return Regex(rootActivityPattern(pkg)).find(rootActivities)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun parseTaskId(dumpsysOutput: String): Int? {
        val m = taskIdRegex.find(dumpsysOutput) ?: return null
        return m.groupValues[1].toIntOrNull()
    }

    // The root activity owns the task. The task affinity can differ from the package
    // The pattern works in grep -E and in Regex. Android 13 and later add one more space after Hist
    private fun rootActivityPattern(pkg: String) =
        "Hist +#0: ActivityRecord\\{[0-9a-f]+ u[0-9]+ ${pkg.replace(".", "\\.")}/[^ }]+\\}? t([0-9]+)"

    /**
     * Convenience wrapper to force-stop a package via ActivityManager.
     */
    override suspend fun forceStop(packageName: String): String {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || pkg.equals("unknown", ignoreCase = true) || !isValidPackageName(pkg)) {
            return "no valid package names"
        }
        return execute("am force-stop --user 0 $pkg")
    }

    override suspend fun forceStop(vararg packageNames: String): String {
        val sb = StringBuilder(64)

        for (i in packageNames.indices) {
            val raw = packageNames[i]
            if (raw.isEmpty()) continue

            val pkg = raw.trim()
            if (pkg.isEmpty()) continue
            if (pkg.equals("unknown", ignoreCase = true)) continue
            if (!isValidPackageName(pkg)) continue

            if (sb.isNotEmpty()) sb.append("; ")
            sb.append("am force-stop --user 0 ").append(pkg)
        }

        if (sb.isEmpty()) return "no valid package names"
        return execute(sb.toString())
    }

    override suspend fun enablePackage(packageName: String): String {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || pkg.equals("unknown", ignoreCase = true) || !isValidPackageName(pkg)) {
            return "no valid package names"
        }
        return execute("pm enable $pkg")
    }

    override suspend fun allowActivateVpnAppOp(packageName: String): String {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || pkg.equals("unknown", ignoreCase = true) || !isValidPackageName(pkg)) {
            return "no valid package names"
        }
        return execute("appops set $pkg ACTIVATE_VPN allow")
    }

    override suspend fun applyRequiredSystemSettings(packageName: String) = execute(
        "settings put global development_settings_enabled 1; " +
            "settings put global enable_freeform_support 1; " +
            "settings put global force_resizable_activities 1; " +
            "appops set $packageName SYSTEM_ALERT_WINDOW allow"
    )

    override suspend fun enableAndLaunchApp(packageName: String, launchActivity: String?): String {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || pkg.equals("unknown", ignoreCase = true) || !isValidPackageName(pkg)) {
            return "no valid package names"
        }
        val enableOut = enablePackage(pkg)
        val trimmedActivity = launchActivity?.trim()?.takeUnless { it.isEmpty() }
        val launchCmd = if (trimmedActivity != null) {
            val fqcn = if (trimmedActivity.startsWith(".")) {
                pkg + trimmedActivity
            } else {
                trimmedActivity
            }
            "am start --user 0 -n $pkg/$fqcn"
        } else {
            "monkey -p $pkg -c android.intent.category.LAUNCHER 1"
        }
        val launchOut = execute(launchCmd)
        return "$enableOut\n$launchOut"
    }

    override suspend fun disableUserPackage(packageName: String): String {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || pkg.equals("unknown", ignoreCase = true) || !isValidPackageName(pkg)) {
            return "no valid package names"
        }
        return execute("pm disable-user --user 0 $pkg")
    }

    override suspend fun minimize(taskId: Int) {
        // Android 8 keeps all windows of one mode in one stack, so one window cannot close alone
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return
        if (taskId == -1 || taskId == 0) return
        // Android 9 and 10 remove a stack. A stack of another task must stay
        val stackId = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            findLegacyStack(taskId)?.takeIf { it.taskCount == 1 }?.id ?: return
        } else taskId
        execute("am stack remove $stackId")
    }

    // An own window has a root activity that the owner app started, or its task id is in ownTaskIds
    // A full screen task stays. The mode check and the removal are one shell command
    override suspend fun removeOwnWindowTasks(
        ownPackage: String,
        ownTaskIds: Set<Int>,
        keepPackages: Set<String>
    ): Set<Int> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return emptySet()
        val dump = execute("dumpsys activity activities | grep -E '\\* Task\\{|Hist +#|launchedFromPackage='")
        val windowTaskIds = mutableSetOf<Int>()
        val rootPackages = mutableMapOf<Int, String>()
        val rootLaunchers = mutableMapOf<Int, String>()
        var rootTaskId: Int? = null
        dump.lineSequence().forEach { line ->
            when {
                line.isTaskHeader() -> if (" type=standard " in line && windowModeRegex.containsMatchIn(line)) {
                    parseTaskId(line)?.let(windowTaskIds::add)
                }

                "Hist " in line -> rootTaskId = rootActivityRegex.find(line)?.let { match ->
                    match.groupValues[2].toInt().also { rootPackages[it] = match.groupValues[1] }
                }

                else -> rootTaskId?.let { taskId ->
                    launchedFromPackageRegex.find(line)?.let { rootLaunchers[taskId] = it.groupValues[1] }
                    rootTaskId = null
                }
            }
        }

        val ownWindowTaskIds = windowTaskIds.filter { taskId ->
            val rootPackage = rootPackages[taskId] ?: return@filter false
            rootPackage != ownPackage && rootPackage !in keepPackages &&
                (rootLaunchers[taskId] == ownPackage || taskId in ownTaskIds)
        }.toSet()
        if (ownWindowTaskIds.isEmpty()) return emptySet()

        val isWindow = "dumpsys activity activities | grep -E \"\\* Task\\{[0-9a-f]+ #\$i \" | head -n 1 | " +
            "grep -qE ' mode=(freeform|multi-window) '"
        val remove = "am stack remove \$i && echo REMOVED=\$i"
        // A skipped task returns a non zero exit code. The final true keeps the output
        val output = execute("for i in ${ownWindowTaskIds.joinToString(" ")}; do $isWindow && $remove; done; true")
        return removedTaskRegex.findAll(output).map { it.groupValues[1].toInt() }.toSet()
    }

    // Android 9 and 10 show the windowing mode on the stack line and the stack id in the task line
    private suspend fun findLegacyStack(taskId: Int): LegacyStack? {
        val dump = execute("dumpsys activity activities | grep -E 'Stack #[0-9]+:|\\* TaskRecord\\{'")
        val stackPattern = Regex("""\* TaskRecord\{[0-9a-f]+ #$taskId [^}]*StackId=(\d+)""")
        val stackId = stackPattern.firstGroup(dump)?.toIntOrNull() ?: return null
        val mode = Regex("""Stack #$stackId: type=standard mode=([\w-]+)""").firstGroup(dump) ?: return null
        val taskCount = Regex("""\* TaskRecord\{[^}]*StackId=$stackId\b""").findAll(dump).count()
        return LegacyStack(stackId, mode, taskCount)
    }

    private fun Regex.firstGroup(input: String) = find(input)?.groupValues?.get(1)

    override suspend fun ensureConnected(): Boolean {
        if (_connectionState.value is AdbConnectionState.Connected) return true
        if (!dataStore.getBooleanPrefFlow(BoolPref.EnableAdbHelper).first()) return false
        if (reconnectJob?.isActive != true) ioScope.launch { reconnect() }
        withTimeoutOrNull(CONNECT_AWAIT_MS) {
            var attemptSeen = false
            _connectionState.first { state ->
                when (state) {
                    is AdbConnectionState.Connected -> true
                    is AdbConnectionState.Connecting -> {
                        attemptSeen = true
                        false
                    }
                    // Error or Disconnected after an attempt means the reconnect failed
                    else -> attemptSeen
                }
            }
        }
        return _connectionState.value is AdbConnectionState.Connected
    }

    // Task ids grow with each new task, so the largest id is the task that was just started
    override suspend fun resizeNewTask(packageName: String, bounds: Rect): Boolean {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || !isValidPackageName(pkg)) return false
        val findTaskId = "dumpsys activity activities | grep -o -E '${rootActivityPattern(pkg)}' | " +
            "grep -o -E '[0-9]+\$' | sort -n | tail -1"
        val resize = "am task resize \"\$i\" ${bounds.left} ${bounds.top} ${bounds.right} ${bounds.bottom}"
        val script = "n=0; r=; " +
            "while [ \$n -lt $NEW_TASK_LOOKUP_ATTEMPTS ]; do i=\$($findTaskId); " +
            "if [ -n \"\$i\" ]; then $resize && r=\$i; break; fi; " +
            "n=\$((n+1)); sleep $NEW_TASK_LOOKUP_INTERVAL_SEC; done; echo \"RESIZED=\$r\""
        return execute(script).contains(Regex("RESIZED=\\d+"))
    }

    override suspend fun getResizeableFullscreenTaskId(packageName: String): Int? {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || pkg.equals("unknown", ignoreCase = true) || !isValidPackageName(pkg)) {
            return null
        }

        val rootActivityRegex = Regex(rootActivityPattern(pkg))
        val dump = execute(
            "dumpsys activity activities | grep -E 'Task\\{|Hist +#|resizeMode=' | $SKIP_FINISHING_ACTIVITIES"
        )
        val taskBlock = dump.lines().splitTaskBlocks().firstOrNull { block ->
            block.any { rootActivityRegex.containsMatchIn(it) }
        } ?: return null
        val header = taskBlock.first()
        val isStandardFullscreen = " type=standard " in header && " mode=fullscreen " in header
        val taskId = parseTaskId(header)?.takeIf { isStandardFullscreen } ?: return null

        val resizeModes = taskBlock.mapNotNull { resizeModeRegex.find(it)?.groupValues?.get(1) }
        return taskId.takeIf { resizeModes.isNotEmpty() && resizeModes.all { it in resizeableModes } }
    }

    private fun List<String>.splitTaskBlocks(): List<List<String>> {
        val blocks = mutableListOf<MutableList<String>>()
        forEach { line ->
            if (line.isTaskHeader()) blocks.add(mutableListOf(line)) else blocks.lastOrNull()?.add(line)
        }
        return blocks
    }

    private fun String.isTaskHeader() = trimStart().startsWith("* Task{")

    override suspend fun moveTaskToWindow(
        taskId: Int,
        windowingMode: Int,
        bounds: Rect,
        setWindowingModeCode: Int
    ): Boolean {
        if (taskId <= 0) return false
        val isFullscreenTask = "dumpsys activity activities | grep -E '\\* Task\\{[0-9a-f]+ #$taskId ' | " +
            "head -n 1 | grep -q ' type=standard mode=fullscreen '"
        val script = "$isFullscreenTask && ${windowingModeScript(taskId, windowingMode, bounds, setWindowingModeCode)}"
        return execute(script).contains(MOVED_TO_WINDOW)
    }

    override suspend fun getTaskWindowingMode(taskId: Int): String? {
        if (taskId <= 0) return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return findLegacyStack(taskId)?.mode
        val header = execute("dumpsys activity activities | grep -E '\\* Task\\{[0-9a-f]+ #$taskId ' | head -n 1")
        return taskWindowingModeRegex.firstGroup(header)
    }

    override suspend fun setTaskWindowingMode(
        taskId: Int,
        windowingMode: Int,
        bounds: Rect,
        setWindowingModeCode: Int
    ): Boolean {
        if (taskId <= 0) return false
        val script = windowingModeScript(taskId, windowingMode, bounds, setWindowingModeCode)
        return execute(script).contains(MOVED_TO_WINDOW)
    }

    private fun windowingModeScript(taskId: Int, windowingMode: Int, bounds: Rect, setWindowingModeCode: Int): String {
        val setWindowingMode = "service call activity_task $setWindowingModeCode i32 $taskId i32 $windowingMode i32 1"
        val resize = "am task resize $taskId ${bounds.left} ${bounds.top} ${bounds.right} ${bounds.bottom}"
        return "r=\$($setWindowingMode) && " +
            "case \"\$r\" in *'Parcel(00000000 00000001'*) $resize && echo $MOVED_TO_WINDOW;; esac"
    }

    override suspend fun moveTaskToFullscreen(taskId: Int, setWindowingModeCode: Int): Boolean {
        if (taskId <= 0) return false
        val reply = execute("service call activity_task $setWindowingModeCode i32 $taskId i32 1 i32 1")
        return reply.contains("Parcel(00000000 00000001")
    }

    override suspend fun focusTaskInBounds(taskId: Int, bounds: Rect): Boolean {
        if (taskId <= 0) return false
        val resize = "am task resize $taskId ${bounds.left} ${bounds.top} ${bounds.right} ${bounds.bottom}"
        return execute("am task focus $taskId && $resize && echo FOCUSED").contains("FOCUSED")
    }

    @Suppress("ReturnCount")
    override suspend fun getForegroundAppPackageName(): String? {
        val windowDump = execute("dumpsys window windows")
        if (windowDump.isNotBlank()) {
            sequenceOf("mFocusedApp=", "mCurrentFocus=")
                .mapNotNull { marker -> extractPackageAroundMarker(windowDump, marker) }
                .firstOrNull { pkg -> !isSystemOverlayPackage(pkg) && isValidPackageName(pkg) }
                ?.let { return it }
        }

        val activityDump = execute("dumpsys activity activities")
        if (activityDump.isBlank()) return null

        val resumedLine = activityDump.lineSequence().firstOrNull { line ->
            line.contains("topResumedActivity") ||
                line.contains("mTopResumedActivity") ||
                line.contains("mResumedActivity") ||
                line.contains("ResumedActivity")
        } ?: return null

        val match = foregroundPackageRegex.find(resumedLine) ?: return null
        val pkg = match.groupValues[1]
        return pkg.takeIf { it.isNotBlank() && !isSystemOverlayPackage(it) && isValidPackageName(it) }
    }

    private fun extractPackageAroundMarker(dump: String, marker: String): String? {
        val idx = dump.indexOf(marker)
        if (idx < 0) return null

        val endExclusive = (idx + 600).coerceAtMost(dump.length)
        val chunk = dump.substring(idx, endExclusive)

        val match = foregroundPackageRegex.find(chunk) ?: return null
        return match.groupValues[1].trim()
    }

    private fun isSystemOverlayPackage(pkg: String): Boolean {
        return pkg == "com.android.systemui" ||
            pkg == "android" ||
            pkg.startsWith("com.android.launcher") ||
            pkg.startsWith("com.google.android.apps.nexuslauncher")
    }

    override suspend fun getRecentTasksFromActivitiesDump(): List<AdbRecentTaskInfo> {
        val dump = execute("dumpsys activity activities")
        if (dump.isBlank()) return emptyList()
        return parseRecentTasksFromActivitiesDump(dump)
    }

    private fun parseRecentTasksFromActivitiesDump(dump: String): List<AdbRecentTaskInfo> {
        val out = ArrayList<AdbRecentTaskInfo>(16)

        var inDisplay0 = false

        var taskId: Int? = null
        var type: String? = null
        var visible = false
        var visibleRequested = false
        var packageName: String? = null
        var topResumed = false
        var activityState: String? = null
        var nowVisible: Boolean? = null
        var lastVisibleTime: String? = null
        var baseDir: String? = null
        var dataDir: String? = null

        fun flushCurrentTask() {
            val id = taskId ?: return
            val t = type ?: return
            val pkg = packageName ?: return
            if (isSystemTask(t, pkg)) return

            out.add(
                AdbRecentTaskInfo(
                    taskId = id,
                    packageName = pkg,
                    visible = visible,
                    visibleRequested = visibleRequested,
                    topResumed = topResumed,
                    activityState = activityState,
                    nowVisible = nowVisible,
                    lastVisibleTime = lastVisibleTime,
                    baseDir = baseDir,
                    dataDir = dataDir
                )
            )
        }

        fun resetForNextTask() {
            taskId = null
            type = null
            visible = false
            visibleRequested = false
            packageName = null
            topResumed = false
            activityState = null
            nowVisible = null
            lastVisibleTime = null
            baseDir = null
            dataDir = null
        }

        for (line in dump.lineSequence()) {
            if (!inDisplay0) {
                if (line.startsWith("Display #0")) {
                    inDisplay0 = true
                }
                continue
            }

            if (line.startsWith("Resumed activities in task display areas")) {
                flushCurrentTask()
                break
            }

            if (line.startsWith("  * Task{")) {
                flushCurrentTask()
                resetForNextTask()

                val headerMatch = taskHeaderRegex.find(line) ?: continue
                taskId = headerMatch.groupValues[1].toIntOrNull()
                type = headerMatch.groupValues[2]

                taskHeaderVisibleRegex.find(line)?.groupValues?.get(1)?.toBooleanStrictOrNull()
                    ?.let { visible = it }
                taskHeaderVisibleRequestedRegex.find(line)?.groupValues?.get(1)
                    ?.toBooleanStrictOrNull()
                    ?.let { visibleRequested = it }

                taskHeaderPackageFromARegex.find(line)?.groupValues?.get(1)
                    ?.let { packageName = it }

                continue
            }

            val id = taskId ?: continue

            if (!topResumed && line.contains("topResumedActivity=") && line.contains("t$id")) {
                topResumed = true
            }

            packageNameLineRegex.find(line)?.groupValues?.get(1)?.let { packageName = it }

            if (activityState == null) {
                activityStateRegex.find(line)?.groupValues?.get(1)?.let { activityState = it }
            }

            if (nowVisible == null) {
                nowVisibleRegex.find(line)?.groupValues?.get(1)?.toBooleanStrictOrNull()
                    ?.let { nowVisible = it }
            }

            if (lastVisibleTime == null) {
                lastVisibleTimeRegex.find(line)?.groupValues?.get(1)?.let { lastVisibleTime = it }
            }

            if (baseDir == null) {
                baseDirRegex.find(line)?.groupValues?.get(1)?.let { baseDir = it }
            }

            if (dataDir == null) {
                dataDirRegex.find(line)?.groupValues?.get(1)?.let { dataDir = it }
            }
        }

        return out
    }

    @Suppress("ReturnCount")
    private fun isSystemTask(type: String, packageName: String): Boolean {
        if (type == "home") return true
        if (packageName == "com.android.systemui") return true

        if (packageName == "com.google.android.apps.nexuslauncher") return true
        if (packageName == "com.google.android.apps.pixel.launcher") return true

        if (packageName.startsWith("com.android.launcher")) return true
        if (packageName.startsWith("com.google.android.apps.launcher")) return true
        if (packageName.endsWith(".launcher")) return true

        return false
    }

    private fun String.toBooleanStrictOrNull(): Boolean? {
        return when (this) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }

    /**
     * Simulates pressing the system Home button via ADB to return to the launcher.
     */
    override suspend fun pressHome() {
        execute("input keyevent KEYCODE_HOME")
    }

    /**
     * Simulates pressing the system Back button via ADB to trigger standard back navigation.
     */
    override suspend fun pressBack() {
        execute("input keyevent KEYCODE_BACK")
    }

    /**
     * Closes connection/socket; idempotent.
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        Timber.d("[ADB] disconnect")
        isManuallyDisconnected = true
        shizukuPermissionRequested = false
        cancelReconnectLoop()

        // Invalidate any in-flight connect/execute so they can't resurrect the connection.
        synchronized(connGuard) {
            connectionEpoch++
        }

        // Close transport immediately, even if a command is currently executing.
        forceCloseNow()

        _connectionState.value = AdbConnectionState.Disconnected

        // Best-effort cleanup under lock; do not wait if a command holds the mutex.
        if (lock.tryLock()) {
            try {
                // No-op: state/refs are already cleared by forceCloseNow().
            } finally {
                lock.unlock()
            }
        }
    }

    /**
     * Executes under lock; propagates exceptions to let caller decide on reconnect.
     */
    private suspend fun executeLocked(command: String): String = lock.withLock {
        val (conn, myEpoch) = synchronized(connGuard) {
            val c = checkNotNull(connection) { "ADB is not connected" }
            c to connectionEpoch
        }

        Timber.d("[ADB] execute: %s", command)

        // If disconnect() happened before we start, abort early.
        synchronized(connGuard) {
            check(connectionEpoch == myEpoch && !isManuallyDisconnected) { "ADB disconnected" }
        }

        val marker = buildDoneMarker()
        val effectiveCommand = appendMarker(command, marker)

        val stream: AdbStream = conn.open("shell:$effectiveCommand")
        return@withLock try {
            val (output, exitCode) = readUntilMarker(stream, marker)

            // If disconnect() happened during execution, do not treat output as valid.
            synchronized(connGuard) {
                check(connectionEpoch == myEpoch && !isManuallyDisconnected) { "ADB disconnected" }
            }

            if (exitCode != 0) {
                throw CommandFailedException(exitCode, output)
            }

            output.also { Timber.d("[ADB] result length=%d", it.length) }
        } finally {
            try {
                stream.close()
            } catch (_: Throwable) {
                // ignore
            }
        }
    }

    // A telnet server can close an idle session while the client still reports a connection
    private suspend fun executeTelnetWithReconnect(command: String): String = try {
        executeTelnetLocked(command)
    } catch (e: TelnetSessionClosedException) {
        if (isManuallyDisconnected) throw e
        Timber.w(e, "[Telnet] session closed, reconnect")
        dropConnectionForRetry(e)
        if (!connectTelnet()) throw e
        executeTelnetLocked(command)
    }

    private suspend fun executeTelnetLocked(command: String): String = lock.withLock {
        val (transport, myEpoch) = synchronized(connGuard) {
            val t = checkNotNull(telnetTransport) { "Telnet is not connected" }
            t to connectionEpoch
        }

        Timber.d("[Telnet] execute: %s", command)

        synchronized(connGuard) {
            check(connectionEpoch == myEpoch && !isManuallyDisconnected) { "Telnet disconnected" }
        }

        val marker = buildDoneMarker()
        val (output, exitCode) = transport.exec(command, marker)

        synchronized(connGuard) {
            check(connectionEpoch == myEpoch && !isManuallyDisconnected) { "Telnet disconnected" }
        }

        if (exitCode != 0) {
            throw CommandFailedException(exitCode, output)
        }

        output.also { Timber.d("[Telnet] result length=%d", it.length) }
    }

    private suspend fun executeShizukuLocked(command: String): String = lock.withLock {
        val (transport, myEpoch) = synchronized(connGuard) {
            val t = checkNotNull(shizukuTransport) { "Shizuku is not connected" }
            t to connectionEpoch
        }

        Timber.d("[Shizuku] execute: %s", command)

        synchronized(connGuard) {
            check(connectionEpoch == myEpoch && !isManuallyDisconnected) { "Shizuku disconnected" }
        }

        val (output, exitCode) = transport.exec(command)

        synchronized(connGuard) {
            check(connectionEpoch == myEpoch && !isManuallyDisconnected) { "Shizuku disconnected" }
        }

        if (exitCode != 0) {
            throw CommandFailedException(exitCode, output)
        }

        output.also { Timber.d("[Shizuku] result length=%d", it.length) }
    }

    private fun buildDoneMarker(): String {
        // Uses nanoTime to avoid collisions without extra allocations/overhead.
        return DONE_PREFIX + System.nanoTime().toString() + ":"
    }

    private fun appendMarker(command: String, marker: String): String {
        // Keeps the original command intact; just appends a deterministic trailer.
        val trimmed = command.trimEnd()
        return if (trimmed.endsWith(";")) {
            "$trimmed echo $marker\$?"
        } else {
            "$trimmed; echo $marker\$?"
        }
    }

    private fun readUntilMarker(stream: AdbStream, marker: String): Pair<String, Int> {
        val out = StringBuilder(256)
        var markerIndex = -1
        while (!stream.isClosed) {
            val chunk = stream.read() ?: break
            if (chunk.isEmpty()) break
            out.append(String(chunk))

            if (markerIndex < 0) {
                markerIndex = out.indexOf(marker)
            }
            if (markerIndex >= 0) {
                val after = out.substring(markerIndex + marker.length)
                val exit = parseLeadingInt(after)
                if (exit != null) {
                    val output = out.substring(0, markerIndex).trimEnd()
                    return output to exit
                }
            }
        }
        throw IOException("ADB stream closed before completion marker")
    }

    private fun parseLeadingInt(value: String): Int? {
        var i = 0
        while (i < value.length && value[i].isWhitespace()) i++
        if (i >= value.length || !value[i].isDigit()) return null

        var num = 0
        while (i < value.length && value[i].isDigit()) {
            num = num * 10 + (value[i] - '0')
            i++
        }
        return num
    }

    private class CommandFailedException(
        val exitCode: Int,
        val output: String,
    ) : IOException("ADB command failed (exit=$exitCode)")

    /**
     * Marks connection as failed and closes resources, without entering Disconnected state.
     */
    private suspend fun dropConnectionForRetry(t: Throwable) {
        lock.withLock {
            if (isManuallyDisconnected) return@withLock
            _connectionState.value = AdbConnectionState.Error(t.message ?: "ADB error")
            safeClose()
        }
    }

    /**
     * Cancels pending reconnect loop after a successful connection or manual disconnect.
     */
    private fun cancelReconnectLoop() {
        val job = reconnectJob
        if (job?.isActive == true) {
            job.cancel()
        }
        reconnectJob = null
    }

    /**
     * Fast in-memory liveness check; not a protocol-level ping.
     */
    private fun isConnectedForPortUnsafe(port: Int): Boolean {
        return when {
            isTelnetMode(port) -> isTelnetConnectedUnsafe()
            isShizukuMode(port) -> isShizukuConnectedUnsafe()
            else -> isAdbConnectedUnsafe()
        }
    }

    private fun isAdbConnectedUnsafe(): Boolean = synchronized(connGuard) {
        val s = socket
        val c = connection
        s != null && !s.isClosed && c != null && !isManuallyDisconnected
    }

    private fun isTelnetConnectedUnsafe(): Boolean = synchronized(connGuard) {
        val t = telnetTransport
        t != null && !t.isClosed() && !isManuallyDisconnected
    }

    private fun isShizukuConnectedUnsafe(): Boolean {
        val published = synchronized(connGuard) { shizukuTransport != null && !isManuallyDisconnected }
        return published && Shizuku.pingBinder()
    }

    private fun forceCloseNow() {
        // Closes transport immediately, without waiting for the main execution lock.
        val toCloseConn: AdbConnection?
        val toCloseSocket: Socket?
        val toCloseTelnet: TelnetShellTransport?
        val toCloseShizuku: ShizukuShellTransport?

        synchronized(connGuard) {
            toCloseConn = connection
            toCloseSocket = socket
            toCloseTelnet = telnetTransport
            toCloseShizuku = shizukuTransport
            connection = null
            socket = null
            telnetTransport = null
            shizukuTransport = null
        }

        runCatching { toCloseConn?.close() }
        runCatching { toCloseSocket?.close() }
        runCatching { toCloseTelnet?.close() }
        toCloseShizuku?.close()
    }

    /**
     * Safely closes and nulls connection/socket.
     */
    private fun safeClose() {
        val toCloseConn: AdbConnection?
        val toCloseSocket: Socket?
        val toCloseTelnet: TelnetShellTransport?
        val toCloseShizuku: ShizukuShellTransport?

        synchronized(connGuard) {
            toCloseConn = connection
            toCloseSocket = socket
            toCloseTelnet = telnetTransport
            toCloseShizuku = shizukuTransport
            connection = null
            socket = null
            telnetTransport = null
            shizukuTransport = null
        }

        try {
            toCloseConn?.close()
        } catch (t: Throwable) {
            Timber.w(t, "[ADB] connection close error")
        }
        try {
            toCloseSocket?.close()
        } catch (t: Throwable) {
            Timber.w(t, "[ADB] socket close error")
        }
        try {
            toCloseTelnet?.close()
        } catch (t: Throwable) {
            Timber.w(t, "[Telnet] socket close error")
        }
        toCloseShizuku?.close()
    }

    private suspend fun connectTelnet(): Boolean = withContext(Dispatchers.IO) {
        lock.withLock {
            val myEpoch = synchronized(connGuard) { connectionEpoch }

            isManuallyDisconnected = false

            val existing = synchronized(connGuard) { telnetTransport }
            if (existing != null && !existing.isClosed()) {
                _connectionState.value = AdbConnectionState.Connected
                Timber.d("[Telnet] connect skipped: already connected")
                cancelReconnectLoop()
                return@withLock true
            }

            _connectionState.value = AdbConnectionState.Connecting
            Timber.d("[Telnet] discovery started")

            try {
                val (endpoint, transport) = telnetDiscovery.open()

                val canPublish = synchronized(connGuard) {
                    connectionEpoch == myEpoch && !isManuallyDisconnected
                }
                if (!canPublish) {
                    runCatching { transport.close() }
                    _connectionState.value = AdbConnectionState.Disconnected
                    return@withLock false
                }

                synchronized(connGuard) {
                    if (connectionEpoch != myEpoch || isManuallyDisconnected) {
                        runCatching { transport.close() }
                        _connectionState.value = AdbConnectionState.Disconnected
                        return@withLock false
                    }
                    runCatching { connection?.close() }
                    runCatching { socket?.close() }
                    connection = null
                    socket = null
                    telnetTransport = transport
                }

                _connectionState.value = AdbConnectionState.Connected
                Timber.d("[Telnet] connected to %s:%d", endpoint.host, endpoint.port)
                cancelReconnectLoop()
                true
            } catch (t: Throwable) {
                _connectionState.value =
                    AdbConnectionState.Error(t.message ?: "Telnet connect error")
                Timber.w(t, "[Telnet] connect error")
                telnetDiscovery.clearCache()
                safeClose()
                scheduleReconnect(host, TELNET_HELPER_PORT, "telnet connect error")
                false
            }
        }
    }

    private suspend fun connectShizuku(): Boolean = withContext(Dispatchers.IO) {
        lock.withLock {
            val myEpoch = synchronized(connGuard) { connectionEpoch }

            isManuallyDisconnected = false

            if (isShizukuConnectedUnsafe()) {
                _connectionState.value = AdbConnectionState.Connected
                Timber.d("[Shizuku] connect skipped: already connected")
                cancelReconnectLoop()
                return@withLock true
            }

            _connectionState.value = AdbConnectionState.Connecting

            try {
                requestShizukuPermissionOnce()
                val transport = ShizukuShellTransport.connect()

                synchronized(connGuard) {
                    if (connectionEpoch != myEpoch || isManuallyDisconnected) {
                        _connectionState.value = AdbConnectionState.Disconnected
                        return@withLock false
                    }
                    runCatching { connection?.close() }
                    runCatching { socket?.close() }
                    telnetTransport?.close()
                    connection = null
                    socket = null
                    telnetTransport = null
                    shizukuTransport = transport
                }

                _connectionState.value = AdbConnectionState.Connected
                Timber.d("[Shizuku] connected")
                cancelReconnectLoop()
                true
            } catch (t: Throwable) {
                _connectionState.value = AdbConnectionState.Error(t.message ?: "Shizuku connect error")
                Timber.w(t, "[Shizuku] connect error")
                safeClose()
                scheduleReconnect(host, SHIZUKU_HELPER_PORT, "shizuku connect error")
                false
            }
        }
    }

    private fun requestShizukuPermissionOnce() {
        if (shizukuPermissionRequested || !Shizuku.pingBinder() || Shizuku.isPreV11()) return
        val isDecided = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED ||
            Shizuku.shouldShowRequestPermissionRationale()
        if (isDecided) return
        shizukuPermissionRequested = true
        Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
    }

    private suspend fun dropDeadShizukuTransport() = lock.withLock {
        if (Shizuku.pingBinder()) return@withLock

        val transport = synchronized(connGuard) {
            shizukuTransport.also { shizukuTransport = null }
        } ?: return@withLock

        transport.close()
        _connectionState.value = AdbConnectionState.Error("Shizuku is not running")
    }

    private fun reconnectShizukuIfSelected() = ioScope.launch {
        if (isShizukuSelected()) reconnect()
    }

    private suspend fun isShizukuSelected() = dataStore.getBooleanPrefFlow(BoolPref.EnableAdbHelper).first() &&
        isShizukuMode(dataStore.getIntPrefFlow(IntPref.AdbHelperPort).first())

    private fun isTelnetMode(port: Int) = port == TELNET_HELPER_PORT

    private fun isShizukuMode(port: Int) = port == SHIZUKU_HELPER_PORT

    private fun isValidPackageName(value: String): Boolean {
        if (!value.contains('.')) return false
        for (ch in value) {
            val ok = ch.isLetterOrDigit() || ch == '_' || ch == '.'
            if (!ok) return false
        }
        return true
    }
}
