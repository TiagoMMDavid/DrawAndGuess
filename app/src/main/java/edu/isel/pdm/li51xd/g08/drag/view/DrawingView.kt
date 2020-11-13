package edu.isel.pdm.li51xd.g08.drag.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import edu.isel.pdm.li51xd.g08.drag.listeners.NewPointListener
import edu.isel.pdm.li51xd.g08.drag.listeners.NewVectorListener
import edu.isel.pdm.li51xd.g08.drag.listeners.SizeChangeListener
import edu.isel.pdm.li51xd.g08.drag.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.model.Point
import edu.isel.pdm.li51xd.g08.drag.model.Vector
import java.util.LinkedList

class DrawingView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val currPath: Path = Path()
    private val currStartPoints: LinkedList<Point> = LinkedList()

    private var viewHeight = 0
    private var viewWidth = 0

    private var newPointListener: NewPointListener? = null
    private var newVectorListener: NewVectorListener? = null
    private var sizeChangeListener: SizeChangeListener? = null

    private companion object {
        val brush: Paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 10f
            style = Paint.Style.STROKE
        }
    }

    fun drawModel(model: Drawing) {
        clearCanvas()
        model.vectors.forEach {
            addVectorToPath(it)
        }

        invalidate()
    }

    fun clearCanvas() {
        currPath.reset()
        currStartPoints.clear()
        invalidate()
    }

    private fun addVectorToPath(vector: Vector) {
        val points = vector.points

        if (points.isNotEmpty()) {
            val point = vector.points[0]
            val x = point.x * viewWidth
            val y = point.y * viewHeight
            currPath.moveTo(x, y)
            currStartPoints.add(Point(x, y))
        }

        for (idx in 1 until points.size) {
            val point = vector.points[idx]
            currPath.lineTo(point.x * viewWidth, point.y * viewHeight)
        }
    }

    fun setOnNewVectorListener(newVectorListener: NewVectorListener) {
        this.newVectorListener = newVectorListener
    }

    fun setOnNewPointListener(newPointListener: NewPointListener) {
        this.newPointListener = newPointListener
    }

    fun setOnSizeChangeListener(sizeChangeListener: SizeChangeListener) {
        this.sizeChangeListener = sizeChangeListener
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(currPath, brush)
        currStartPoints.forEach {
            canvas?.drawPoint(it.x, it.y, brush)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isEnabled) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    currPath.moveTo(event.x, event.y)
                    currStartPoints.add(Point(event.x, event.y))
                    newVectorListener?.onNewVector(event.x / viewWidth, event.y / viewHeight)
                }
                MotionEvent.ACTION_MOVE -> {
                    currPath.lineTo(event.x, event.y)
                    newPointListener?.onNewPoint(event.x / viewWidth, event.y / viewHeight)
                }
            }
            invalidate()
        }
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewHeight = h
        viewWidth = w
        sizeChangeListener?.onSizeChange()
    }
}