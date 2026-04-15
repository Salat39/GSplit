package presentation

import android.os.Looper

fun inMainThread() = Looper.myLooper() == Looper.getMainLooper()
