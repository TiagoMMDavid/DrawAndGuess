package edu.isel.pdm.li51xd.g08.drag.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

const val SWIPE_DEADZONE_RADIUS = 150

fun interface SwipeLeftListener {
    fun onSwipeLeft()
}
fun interface SwipeRightListener {
    fun onSwipeRight()
}

class SwipeActionView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var startX = 0
    private var startY = 0

    private var swipeLeftListener: SwipeLeftListener? = null
    var isSwipeLeftEnabled = true

    private var swipeRightListener: SwipeRightListener? = null
    var isSwipeRightEnabled = true

    fun setOnSwipeLeft(listener: SwipeLeftListener) {
        swipeLeftListener = listener
    }

    fun setOnSwipeRight(listener: SwipeRightListener) {
        swipeRightListener = listener
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x.toInt()
                startY = event.y.toInt()
            }
            MotionEvent.ACTION_UP -> {
                var x = event.x.toInt()
                var y = event.y.toInt()
                x -= startX
                y -= startY
                if (kotlin.math.abs(x) <= SWIPE_DEADZONE_RADIUS && kotlin.math.abs(y) <= SWIPE_DEADZONE_RADIUS) return false

                // X axis movements
                if (kotlin.math.abs(x) > kotlin.math.abs(y)) {
                    if (x > 0 && isSwipeRightEnabled)
                        swipeRightListener?.onSwipeRight()
                    else if (x < 0 && isSwipeLeftEnabled)
                        swipeLeftListener?.onSwipeLeft()
                } else {
                    // Vertical swipes
                }
            }
        }
        return true
    }
}