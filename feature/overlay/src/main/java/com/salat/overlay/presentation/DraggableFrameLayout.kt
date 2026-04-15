package com.salat.overlay.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import android.widget.FrameLayout

// Custom draggable container that uses onInterceptTouchEvent to determine when
// to start dragging and calculates the offset relative to the touch point.
class DraggableFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val isDragEnabled: Boolean = true
) : FrameLayout(context, attrs) {

    // Reference to window parameters, passed from outside.
    var wmLayoutParams: WindowManager.LayoutParams? = null

    private var downTime: Long = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var initialX: Int = 0
    private var initialY: Int = 0

    // Use system timeout for long press.
    private val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()

    // Movement threshold.
    private val dragThreshold = 10f

    // Flag indicating that dragging has started.
    private var dragging = false

    private var onMoveListener: ((x: Int, y: Int) -> Unit)? = null

    @Suppress("ReturnCount")
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isDragEnabled) return false
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downTime = ev.eventTime
                initialTouchX = ev.rawX
                initialTouchY = ev.rawY
                // Store the initial window position from wmLayoutParams.
                initialX = wmLayoutParams?.x ?: 0
                initialY = wmLayoutParams?.y ?: 0
                dragging = false
                // Do not intercept the event – let the child element handle it.
                return false
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = ev.rawX - initialTouchX
                val dy = ev.rawY - initialTouchY
                // If enough time has passed or movement exceeds the threshold, start dragging.
                if (ev.eventTime - downTime >= longPressTimeout ||
                    (dx * dx + dy * dy) > dragThreshold * dragThreshold
                ) {
                    dragging = true
                    return true // Intercept events to start dragging.
                }
                return false
            }

            else -> return false
        }
    }

    @Suppress("ReturnCount")
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDragEnabled) return super.onTouchEvent(event)
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = wmLayoutParams ?: return super.onTouchEvent(event)
        if (dragging) {
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    // The new position is calculated so that the point where the user grabbed the window
                    // remains inside the window.
                    params.x = (event.rawX - (initialTouchX - initialX)).toInt()
                    params.y = (event.rawY - (initialTouchY - initialY)).toInt()
                    onMoveListener?.invoke(params.x, params.y)
                    wm.updateViewLayout(this, params)
                    return true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    dragging = false
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun setOnMoveListener(listener: ((x: Int, y: Int) -> Unit)?) {
        onMoveListener = listener
    }
}
