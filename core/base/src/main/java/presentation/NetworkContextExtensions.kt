package presentation

import android.content.Context
import android.os.Build
import android.webkit.WebSettings

private const val BASE_USER_AGENT = "%BASE_USER_AGENT%"
private const val VERSION_CODE = "%VERSION_CODE%"

/**
 * Create user agent by pattern
 */
fun Context.buildUserAgent(mask: String? = null): String {
    mask?.let { userAgent ->
        return userAgent
            .replace(BASE_USER_AGENT, getUserAgent())
            .replace(VERSION_CODE, getVersionCode().toString())
    }
    return getUserAgent()
}

/**
 * Get WebView or System user agent string
 */
private fun Context.getUserAgent(): String = runCatching {
    WebSettings.getDefaultUserAgent(this)
}.getOrElse {
    System.getProperty("http.agent") ?: ""
}

/**
 * Get app build code
 */
private fun Context.getVersionCode(): Long = runCatching {
    val packageInfo = packageManager.getPackageInfo(packageName, 0)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // From API 28 onward use long version codes
        packageInfo.longVersionCode
    } else {
        // Deprecated in API 28
        @Suppress("DEPRECATION")
        packageInfo.versionCode.toLong()
    }
}.getOrDefault(0)
