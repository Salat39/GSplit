package presentation

import android.util.SparseArray

inline fun <T> SparseArray<T>.getOrPut(key: Int, defaultValue: () -> T): T {
    val value = this.get(key)
    return if (value == null) {
        val newValue = defaultValue()
        this.put(key, newValue)
        newValue
    } else {
        value
    }
}
