package edu.isel.pdm.li51xd.g08.drag.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import edu.isel.pdm.li51xd.g08.drag.model.Draw

class DrawView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val currPath : Path = Path()

    private companion object {
        val brush: Paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 8f
            style = Paint.Style.STROKE
        }
    }

    fun drawModel(model: Draw) {
        currPath.set(model.path)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(currPath, brush)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                currPath.moveTo(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                currPath.lineTo(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> { }
        }
        invalidate()
        return true
    }
}