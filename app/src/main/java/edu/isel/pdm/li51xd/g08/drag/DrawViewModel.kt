package edu.isel.pdm.li51xd.g08.drag

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import edu.isel.pdm.li51xd.g08.drag.model.GameState
import edu.isel.pdm.li51xd.g08.drag.model.Point
import edu.isel.pdm.li51xd.g08.drag.model.Vector

const val SAVED_STATE_KEY = "DRAG.State"

class DrawViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val game: GameState by lazy {
        savedState[SAVED_STATE_KEY] ?: GameState()
    }

    fun save() {
        savedState[SAVED_STATE_KEY] = game
    }

    fun addPoint(x: Float, y: Float, initial: Boolean) {
        val currDrawing = game.currentDrawing
        var currVector = currDrawing.currVector

        if (initial && currVector.points.isNotEmpty()) {
            currDrawing.vectors.add(currVector)
            currDrawing.currVector = Vector()
            currVector = currDrawing.currVector
        }

        currVector.points.add(Point(x, y))
        save()
    }
}