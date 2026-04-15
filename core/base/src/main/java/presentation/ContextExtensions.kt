@file:Suppress("unused")

package presentation

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import timber.log.Timber

fun Context.setKeyboard(condition: Boolean, clearFocus: Boolean, targetView: View? = null) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    if (condition) {
        targetView?.let {
            if (!clearFocus) it.requestFocus()
            imm?.showSoftInput(it, 0)
        }
    } else {
        if (targetView == null) {
            (this as? Activity)?.currentFocus?.let { view ->
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
                if (clearFocus) view.clearFocus()
            }
        } else {
            imm?.hideSoftInputFromWindow(targetView.windowToken, 0)
            if (clearFocus) targetView.clearFocus()
        }
    }
}

fun Context.hideKeyboard() {
    setKeyboard(condition = false, clearFocus = true)
}

fun Context.showKeyboard(view: View) {
    setKeyboard(condition = true, clearFocus = false, targetView = view)
}

fun Context.vibrate(duration: Long = 60) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION") getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    if (vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    duration,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            // Deprecated in API 26
            @Suppress("DEPRECATION") vibrator.vibrate(duration)
        }
    }
}

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun Context.openMarketUrl(url: String?) {
    url ?: return
    val params = url.parseUrlParams()
    val id = params["id"]

    if (id != null) {
        try {
            val intentUri = if ("/dev?" in url) {
                // Developer page
                Uri.parse("market://dev?id=$id")
            } else {
                // App page
                Uri.parse("market://details?id=$id")
            }
            startActivity(Intent(Intent.ACTION_VIEW, intentUri))
        } catch (e: ActivityNotFoundException) {
            Timber.e("Market app not found, using fallback URL", e)
            val fallbackUri = if ("/dev?" in url) {
                Uri.parse("https://play.google.com/store/apps/dev?id=$id")
            } else {
                Uri.parse("https://play.google.com/store/apps/details?id=$id")
            }
            startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
        }
    } else {
        openInBrowser(url)
    }
}

fun Context.openApp(packageName: String?) = packageName?.let { id ->
    val launchIntent = packageManager.getLaunchIntentForPackage(id)
    if (launchIntent != null) {
        startActivity(launchIntent)
    }
}

fun Context.openAppOrMarket(packageName: String?) = packageName?.let { id ->
    val launchIntent = packageManager.getLaunchIntentForPackage(id)
    if (launchIntent != null) {
        startActivity(launchIntent)
    } else {
        try {
            val intentUri = Uri.parse("market://details?id=$id")
            val marketIntent = Intent(Intent.ACTION_VIEW, intentUri).apply {
                setPackage("com.android.vending")
            }
            startActivity(marketIntent)
        } catch (e: ActivityNotFoundException) {
            Timber.e("Market app not found, using fallback URL", e)
            val fallbackUri = Uri.parse("https://play.google.com/store/apps/details?id=$id")
            startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
        }
    }
}

fun Context.openAppInMarketOrBrowser(packageName: String, fallbackUrl: String) {
    val marketUri = Uri.parse("market://details?id=$packageName")
    val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
        setPackage("com.android.vending")
    }

    if (marketIntent.resolveActivity(packageManager) != null) {
        startActivity(marketIntent)
    } else {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
        startActivity(browserIntent)
    }
}

fun Context.openInBrowser(url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    } catch (_: Exception) {
    }
}

fun Context.openYouTubeVideo(videoId: String) {
    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(videoId.generateYouTubeLink())
        )
        startActivity(intent)
    } catch (_: Exception) {
    }
}

fun Context.openFacebookUrl(url: String?) {
    url ?: return

    val uri = try {
        if (isPackageInstalled("com.facebook.katana")) {
            Uri.parse("fb://facewebmodal/f?href=$url")
        } else {
            Uri.parse(url)
        }
    } catch (e: Exception) {
        Timber.e("Parse url fail", e)
        Uri.parse(url)
    }

    try {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e("Open intent fail", e)
        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(fallbackIntent)
    }
}

fun Context.openMarketUri(packageName: String = this.packageName) {
    val marketUri = Uri.parse("market://details?id=$packageName")
    val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
    try {
        startActivity(marketIntent)
        Timber.d("openMarketUri: $marketIntent")
    } catch (_: ActivityNotFoundException) {
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
        startActivity(webIntent)
        Timber.d("openMarketUri: $webIntent")
    }
}

fun Context.sendEmail(email: String, subject: String? = null): Boolean {
    return try {
        val builder = Uri.parse("mailto:$email").buildUpon()
        subject?.let { builder.appendQueryParameter("subject", it) }
        val intent = Intent(Intent.ACTION_SENDTO, builder.build())
        startActivity(Intent.createChooser(intent, null))
        true
    } catch (e: Exception) {
        Timber.e(e, "Failed to send email to $email")
        false
    }
}

fun Context.phoneCall(phone: String): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
        startActivity(Intent.createChooser(intent, null))
        true
    } catch (e: Exception) {
        Timber.e(e, "Failed to initiate phone call to $phone")
        false
    }
}

fun Context.phoneSms(phone: String): Boolean {
    return try {
        // Remove "tel:" prefix if it exists, and sanitize the phone number
        val sanitizedNumber = phone.removePrefix("tel:").replace("\\s".toRegex(), "")
        if (sanitizedNumber.isEmpty()) {
            return false
        }

        // Create a URI for SMS-compatible apps
        val uri = Uri.parse("smsto:$sanitizedNumber")

        // Create an Intent with ACTION_SENDTO
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = uri
        }

        // Launch the chooser to let the user pick an app
        startActivity(Intent.createChooser(intent, "Open with"))
        true // Return true if the chooser was successfully launched
    } catch (e: Exception) {
        // Log the error and return false if something went wrong
        Timber.e(e, "Failed to open phone number in apps: $phone")
        false
    }
}

fun Context.isPackageInstalled(packageName: String): Boolean = try {
    packageManager.getPackageInfo(packageName, 0)
    true
} catch (_: PackageManager.NameNotFoundException) {
    false
}

fun Context.openPackage(pkg: String): Boolean = try {
    val intent = packageManager.getLaunchIntentForPackage(pkg)
    startActivity(intent)
    true
} catch (_: Exception) {
    false
}

fun Context.openDefaultEmailApp(): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_EMAIL)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        true
    } catch (_: Exception) {
        false
    }
}

fun Context.getAppFullName(): String {
    // Get the package manager instance
    val packageManager: PackageManager = packageManager
    // Get the package name
    val packageName: String = packageName

    try {
        // Get the package info for the current app
        val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)

        // Get the app name
        val appName: String = applicationInfo.loadLabel(packageManager).toString()

        // Get the version name
        val versionName: String = packageInfo.versionName ?: ""

        // Get the version code, compatible with all API levels
        val versionCode: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }

        // Combine app name, version name, and version code into a single string
        return "$appName v$versionName ($versionCode)"
    } catch (e: PackageManager.NameNotFoundException) {
        Timber.e("App name fail", e)
    }

    // Return an empty string in case of an error
    return ""
}

fun Context.shareImage(bitmap: Bitmap, text: String? = null, tempFileName: String = "shared_image") {
    try {
        val shareDir = File(cacheDir, "share")
        if (!shareDir.exists()) { // create share subfolder
            shareDir.mkdirs()
        } else if (shareDir.exists() && shareDir.isDirectory) { // clear share folder
            shareDir.listFiles()?.forEach { file ->
                if (file.isFile && file.exists()) {
                    file.delete()
                }
            }
        }

        val file = File(shareDir, "${tempFileName}_${(System.currentTimeMillis() / 1000)}.png")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        val imageUri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imageUri)
            text?.let { shareText -> putExtra(Intent.EXTRA_TEXT, shareText) }
            type = "image/png"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, null)
        startActivity(chooser)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun Context.createTempUri(bitmap: Bitmap, tempFileName: String = "temp_image"): Uri? {
    return try {
        val tempDir = File(cacheDir, "temp")
        if (!tempDir.exists()) { // create temp subfolder
            tempDir.mkdirs()
        } else if (tempDir.exists() && tempDir.isDirectory) { // clear temp folder
            tempDir.listFiles()?.forEach { file ->
                if (file.isFile && file.exists()) {
                    file.delete()
                }
            }
        }

        val file = File(tempDir, "${tempFileName}_${(System.currentTimeMillis() / 1000)}.png")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()

        FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
}

fun Context.isFreeformModeEnabled(): Boolean {
    return try {
        packageManager.hasSystemFeature(PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT) ||
            (Settings.Global.getInt(contentResolver, "enable_freeform_support", 0) == 1)
    } catch (_: Exception) {
        false
    }
}

fun Context.isCanDrawOverlays() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    Settings.canDrawOverlays(this)
} else {
    true
}

fun Context.openFreeformModeSettings() {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e)
        toast("Activate developer mode")
    }
}

fun Context.openAboutDeviceSettings() {
    try {
        val intent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun Context.openOverlayPermissionSettings() {
    try {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun Context.openAccessibilitySettings() = try {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
} catch (e: Exception) {
    Timber.e(e)
}

fun Context.isAccessibilityServiceEnabled(serviceCanonicalName: String): Boolean {
    val expectedComponentName = "$packageName/$serviceCanonicalName"
    val enabledServicesSetting = Settings.Secure.getString(
        contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    if (!enabledServicesSetting.isNullOrEmpty()) {
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServicesSetting)
        while (splitter.hasNext()) {
            val enabledService = splitter.next()
            if (enabledService.equals(expectedComponentName, ignoreCase = true)) {
                return true
            }
        }
    }
    return false
}

fun Context.isAccessibilityServiceEnabled(service: Class<out AccessibilityService>): Boolean {
    val expectedComponentName = ComponentName(this, service).flattenToString()
    val enabledServicesSetting = Settings.Secure.getString(
        contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return enabledServicesSetting?.split(":")?.any {
        it.equals(expectedComponentName, ignoreCase = true)
    } ?: false
}

fun Context.openAppSystemSettings(packageName: String) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:$packageName".toUri()
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun Context.isDeveloperModeEnabled(): Boolean {
    val devOptions = Settings.Global.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
    return devOptions == 1
}

fun Context.toast(text: String) {
    try {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Timber.e(e)
    }
}
