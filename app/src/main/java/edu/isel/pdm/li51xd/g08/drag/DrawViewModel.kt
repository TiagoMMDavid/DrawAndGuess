package edu.isel.pdm.li51xd.g08.drag

import android.graphics.Matrix
import android.graphics.Path
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import edu.isel.pdm.li51xd.g08.drag.model.GameState
import edu.isel.pdm.li51xd.g08.drag.model.Point

const val SAVED_STATE_KEY = "DRAG.State"

class DrawViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val game : GameState by lazy {
        savedState[SAVED_STATE_KEY] ?: GameState()
    }

    fun save() {
        savedState[SAVED_STATE_KEY] = game
    }

    // TODO: FIX SCALING
    fun getScaleMatrix(sizeX: Float, sizeY: Float): Matrix {
        val currentDrawing = game.currentDrawing

        val matrix = Matrix()
        val oldX = currentDrawing.sizeX
        val oldY = currentDrawing.sizeY
        currentDrawing.sizeX = sizeX
        currentDrawing.sizeY = sizeY
        var scaleX = sizeX/oldX
        var scaleY = sizeY/oldY
        if (scaleX > 1 && scaleY > 1) {
            scaleX = 1f
            scaleY = 1f
        }
        if (!(oldX == 0f || oldY == 0f) ) {
            matrix.setScale(scaleX, scaleY)
        }

        save()
        return matrix
    }

    fun addPoint(x: Float, y: Float, initial: Boolean) {
        game.currentDrawing.points.add(Point(x, y, initial))
        save()
    }
}