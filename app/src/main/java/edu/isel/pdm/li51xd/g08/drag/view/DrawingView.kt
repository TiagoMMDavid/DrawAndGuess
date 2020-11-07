package edu.isel.pdm.li51xd.g08.drag.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import edu.isel.pdm.li51xd.g08.drag.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.utils.DrawingListener


class DrawingView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val currPath : Path = Path()
    private var drawingListener: DrawingListener? = null

    private companion object {
        val brush: Paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 8f
            style = Paint.Style.STROKE
        }
    }

    fun drawModel(model: Drawing, matrix: Matrix = Matrix()) {
        currPath.reset()
        model.points.forEach {
            if (it.isInitial) {
                currPath.moveTo(it.x, it.y)
            } else {
                currPath.lineTo(it.x, it.y)
            }
        }
        // TODO: FIX SCALING
        currPath.transform(matrix)
        invalidate()
    }

    fun setOnDrawChangeListener(drawingListener: DrawingListener) {
        this.drawingListener = drawingListener
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(currPath, brush)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                currPath.moveTo(event.x, event.y)
                drawingListener?.onNewPoint(event.x, event.y, true)
            }
            MotionEvent.ACTION_MOVE -> {
                currPath.lineTo(event.x, event.y)
                drawingListener?.onNewPoint(event.x, event.y, false)
            }
        }
        invalidate()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        drawingListener?.onSizeChange(w.toFloat(), h.toFloat())
    }
}