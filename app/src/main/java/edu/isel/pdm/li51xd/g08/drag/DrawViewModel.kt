package edu.isel.pdm.li51xd.g08.drag

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import edu.isel.pdm.li51xd.g08.drag.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.model.GameState
import edu.isel.pdm.li51xd.g08.drag.model.Point
import edu.isel.pdm.li51xd.g08.drag.model.Vector
import edu.isel.pdm.li51xd.g08.drag.model.Word
import edu.isel.pdm.li51xd.g08.drag.utils.GameListener

const val SAVED_STATE_KEY = "DRAG.State"

class DrawViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val game: GameState by lazy {
        savedState[SAVED_STATE_KEY] ?: GameState()
    }

    private var gameListener: GameListener? = null

    fun setOnStateChangeListener(gameListener: GameListener) {
        this.gameListener = gameListener
    }

    fun startGame() {
        savedState[SAVED_STATE_KEY] = game
        gameListener?.onStateChange(game.state)
    }

    fun defineWord(word: String) {
        game.drawGuesses.add(Word(word))

        game.state = GameState.State.DRAWING
        gameListener?.onStateChange(game.state)
    }

    fun defineDrawing() {
        game.drawGuesses.add(game.currentDrawing)

        game.state = GameState.State.GUESSING
        gameListener?.onStateChange(game.state)
    }

    fun defineGuess(word: String) {
        game.drawGuesses.add(Word(word))
        game.currentDrawing = Drawing()

        game.state = GameState.State.DRAWING
        gameListener?.onStateChange(game.state)
    }

    fun addPoint(x: Float, y: Float, initial: Boolean) {
        val currDrawing = game.currentDrawing

        if (initial) {
            currDrawing.vectors.add(Vector())
        }

        currDrawing.vectors.last.points.add(Point(x, y))
    }
}