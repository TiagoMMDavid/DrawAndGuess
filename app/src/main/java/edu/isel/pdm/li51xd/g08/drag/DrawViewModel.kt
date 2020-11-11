package edu.isel.pdm.li51xd.g08.drag

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import edu.isel.pdm.li51xd.g08.drag.listeners.GameListener
import edu.isel.pdm.li51xd.g08.drag.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.model.GameState
import edu.isel.pdm.li51xd.g08.drag.model.Point
import edu.isel.pdm.li51xd.g08.drag.model.Vector
import edu.isel.pdm.li51xd.g08.drag.model.Word

const val GAME_STATE_KEY = "DRAG.GameState"

class DrawViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val game: GameState by lazy {
        savedState[GAME_STATE_KEY] ?: GameState()
    }
    val config: GameConfiguration by lazy {
        savedState[GAME_CONFIGURATION_KEY] ?: GameConfiguration()
    }

    private var gameListener: GameListener? = null

    fun setOnStateChangeListener(gameListener: GameListener) {
        this.gameListener = gameListener
    }

    fun startGame() {
        savedState[GAME_STATE_KEY] = game
        gameListener?.onStateChange(game.state)
    }

    fun defineWord(word: String) {
        game.drawGuesses.add(Word(word))

        game.state = GameState.State.DRAWING
        gameListener?.onStateChange(game.state)
    }

    fun defineDrawing() {
        game.drawGuesses.add(game.currentDrawing)

        if (game.drawGuesses.size == config.playerCount) {
            game.state = GameState.State.RESULTS
        } else {
            game.state = GameState.State.GUESSING
        }

        gameListener?.onStateChange(game.state)

    }

    fun defineGuess(word: String) {
        game.drawGuesses.add(Word(word))
        game.currentDrawing = Drawing()

        if (game.drawGuesses.size == config.playerCount) {
            game.state = GameState.State.RESULTS
        } else {
            game.state = GameState.State.DRAWING
        }

        gameListener?.onStateChange(game.state)
    }

    fun addVectorToModel(x: Float, y: Float) {
        val vectors = game.currentDrawing.vectors
        vectors.add(Vector())
        vectors.last.points.add(Point(x, y))
    }

    fun addPointToModel(x: Float, y: Float) {
        game.currentDrawing.vectors.last.points.add(Point(x, y))
    }
}