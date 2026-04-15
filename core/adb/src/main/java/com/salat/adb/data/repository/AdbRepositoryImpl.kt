package com.salat.adb.data.repository

import android.util.Base64
import com.salat.adb.BuildConfig
import com.salat.adb.data.entity.AdbConnectionState
import com.salat.adb.domain.repository.AdbRepository
import com.salat.preferences.domain.DataStoreRepository
import com.salat.preferences.domain.entity.BoolPref
import com.salat.preferences.domain.entity.IntPref
import com.tananaev.adblib.AdbBase64
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import com.tananaev.adblib.AdbStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber

class AdbRepositoryImpl(private val dataStore: DataStoreRepository) : AdbRepository {

    companion object {
        private const val TIMEOUT_MS = 5_000
        private const val RECONNECT_DELAY_MS = 3_000L
        private const val MAX_RECONNECT_RETRIES = 5

        private const val DONE_PREFIX = "__ADB_DONE__:"
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

    private val _connectionState = MutableStateFlow<AdbConnectionState>(AdbConnectionState.Disconnected)
    override val connectionState: StateFlow<AdbConnectionState> = _connectionState.asStateFlow()

    @Volatile
    private var socket: Socket? = null

    @Volatile
    private var connection: AdbConnection? = null

    private val taskIdRegex = Regex(
        pattern = """\bTask\{[^}]*#(\d+)\b""",
        options = setOf(RegexOption.MULTILINE)
    )

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
                    if (dataStore.load(BoolPref.EnableAdbHelper)) {
                        reconnect()
                    }
                }
            }
        }
    }

    /**
     * Connects to adbd at host:port with ephemeral RSA keys; idempotent.
     */
    suspend fun connect(host: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        lock.withLock {
            // Snapshot epoch to prevent resurrecting connection after disconnect().
            val myEpoch = synchronized(connGuard) { connectionEpoch }

            isManuallyDisconnected = false

            if (isConnectedUnsafe()) {
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

                val crypto: AdbCrypto = AdbCrypto.generateAdbKeyPair(base64)
                val conn = AdbConnection.create(s, crypto)
                conn.connect()

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
                    socket = s
                    connection = conn
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

    /**
     * Ensures background reconnect is attempted when enabled.
     */
    private suspend fun reconnect() {
        isManuallyDisconnected = false

        if (_connectionState.value is AdbConnectionState.Disconnected) {
            // Keep state machine simple: reconnect() is the only entry that can leave Disconnected.
            _connectionState.value = AdbConnectionState.Connecting
        }

        val port = dataStore.load(IntPref.AdbHelperPort)
        if (!isConnectedUnsafe()) {
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
            Timber.d("[ADB] reconnect suppressed: manual disconnect/state disconnected (%s)", reason)
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
                    if (!dataStore.load(BoolPref.EnableAdbHelper)) {
                        disconnect()
                        break
                    }
                    if (isConnectedUnsafe()) {
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
        if (!dataStore.load(BoolPref.EnableAdbHelper)) {
            disconnect()
            return@withContext "ADB helper disabled"
        }

        // If disconnected intentionally, do not attempt any background reconnects.
        if (isManuallyDisconnected || _connectionState.value is AdbConnectionState.Disconnected) {
            return@withContext "ADB disconnected"
        }

        val port = dataStore.load(IntPref.AdbHelperPort)

        if (!isConnectedUnsafe()) {
            val ok = connect(host, port)
            if (!ok) return@withContext "ADB connect failed"
        }

        if (command.isEmpty()) return@withContext "empty command"

        try {
            executeLocked(command)
        } catch (t: CommandFailedException) {
            // Command-level failure: connection is alive (marker reached), no reconnect required.
            Timber.w(t, "[ADB] command failed (exit=%d), output=%s", t.exitCode, t.output.trim().takeLast(300))
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

    override suspend fun isAppInFreeform(packageName: String): Boolean? {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || pkg.equals("unknown", ignoreCase = true) || !isValidPackageName(pkg)) {
            return false
        }

        if (getTaskId(packageName) == null) return null

        val r = execute(
            "dumpsys activity activities | " +
                "grep -i -E \"WindowingMode|mWindowingMode|windowingMode|$pkg\""
        )
        return r.contains("mode=freeform ")
    }

    override suspend fun getTaskId(packageName: String): Int? {
        val pkg = packageName.trim()
        if (pkg.isEmpty() || pkg.equals("unknown", ignoreCase = true) || !isValidPackageName(pkg)) {
            return null
        }
        return parseTaskId(execute("dumpsys activity activities | grep -E \"Task\\{.*$packageName|taskId=\""))
    }

    private fun parseTaskId(dumpsysOutput: String): Int? {
        val m = taskIdRegex.find(dumpsysOutput) ?: return null
        return m.groupValues[1].toIntOrNull()
    }

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

    override suspend fun minimize(taskId: Int) {
        if (taskId == -1 || taskId == 0) return
        execute("am stack remove $taskId")
    }

    /**
     * Closes connection/socket; idempotent.
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        Timber.d("[ADB] disconnect")
        isManuallyDisconnected = true
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
    private fun isConnectedUnsafe(): Boolean = synchronized(connGuard) {
        val s = socket
        val c = connection
        s != null && !s.isClosed && c != null && !isManuallyDisconnected
    }

    private fun forceCloseNow() {
        // Closes transport immediately, without waiting for the main execution lock.
        val toCloseConn: AdbConnection?
        val toCloseSocket: Socket?

        synchronized(connGuard) {
            toCloseConn = connection
            toCloseSocket = socket
            connection = null
            socket = null
        }

        runCatching { toCloseConn?.close() }
        runCatching { toCloseSocket?.close() }
    }

    /**
     * Safely closes and nulls connection/socket.
     */
    private fun safeClose() {
        val toCloseConn: AdbConnection?
        val toCloseSocket: Socket?

        synchronized(connGuard) {
            toCloseConn = connection
            toCloseSocket = socket
            connection = null
            socket = null
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
    }

    private fun isValidPackageName(value: String): Boolean {
        if (!value.contains('.')) return false
        for (ch in value) {
            val ok = ch.isLetterOrDigit() || ch == '_' || ch == '.'
            if (!ok) return false
        }
        return true
    }
}
