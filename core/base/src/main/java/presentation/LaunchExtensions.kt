package presentation

import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import timber.log.Timber

fun Context.sendYmAutoPlayCompat() {
    try {
        val intent = Intent("action.startPlayback").apply {
            setClassName("ru.yandex.music", "ru.yandex.music.main.MainScreenActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            setPackage("ru.yandex.music")
        }
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun Context.sendMurglarAutoPlayCompat() {
    try {
        val intent = Intent("com.badmanners.murglar2.action.SHOW_PLAYER").apply {
            setClassName("com.badmanners.murglar2", "com.badmanners.murglar.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun Context.sendVkxAutoPlayCompat() {
    try {
        val intent = Intent("ua.itaysonlab.vkx.action.OPEN_PLAYER").apply {
            setClassName("ua.itaysonlab.vkx", "ua.itaysonlab.vkx.activity.AppActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun Context.sendPlayerAutoPlay(packageName: String) {
    try {
        val keyEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY)
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            putExtra(Intent.EXTRA_KEY_EVENT, keyEvent)
            setPackage(packageName)
        }
        sendOrderedBroadcast(intent, null)
    } catch (e: Exception) {
        Timber.e(e)
    }
}
