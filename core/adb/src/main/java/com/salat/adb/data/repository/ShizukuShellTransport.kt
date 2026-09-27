package com.salat.adb.data.repository

import android.content.pm.PackageManager
import android.os.ParcelFileDescriptor
import moe.shizuku.server.IRemoteProcess
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku

internal class ShizukuShellTransport private constructor() {

    @Volatile
    private var running: IRemoteProcess? = null

    @Volatile
    private var closed = false

    fun exec(command: String): Pair<String, Int> {
        val binder = checkNotNull(Shizuku.getBinder()) { "Shizuku is not running" }
        val remote = IShizukuService.Stub.asInterface(binder)
            .newProcess(arrayOf("/system/bin/sh", "-c", "exec 2>&1; $command"), null, null)
        running = remote
        try {
            check(!closed) { "Shizuku transport is closed" }
            val output = ParcelFileDescriptor.AutoCloseInputStream(remote.inputStream)
                .bufferedReader()
                .use { it.readText() }
            return output.trimEnd() to remote.waitFor()
        } finally {
            running = null
            runCatching { remote.destroy() }
        }
    }

    fun close() = runCatching {
        closed = true
        running?.destroy()
    }

    companion object {
        fun connect(): ShizukuShellTransport {
            check(Shizuku.pingBinder()) { "Shizuku is not running" }
            check(!Shizuku.isPreV11()) { "Shizuku version is not supported" }
            check(Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                "Shizuku permission required"
            }
            return ShizukuShellTransport().also {
                check(it.exec("true").second == 0) { "Shizuku shell is not available" }
            }
        }
    }
}
