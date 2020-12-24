package edu.isel.pdm.li51xd.g08.drag.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import edu.isel.pdm.li51xd.g08.drag.game.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_CONFIGURATION_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_STATE_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState
import edu.isel.pdm.li51xd.g08.drag.game.model.Point
import edu.isel.pdm.li51xd.g08.drag.game.model.Vector
import edu.isel.pdm.li51xd.g08.drag.game.model.Word
import edu.isel.pdm.li51xd.g08.drag.listeners.GameListener
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY

class DrawViewModel(private val savedState: SavedStateHandle) : ViewModel() {

    val game: GameState by lazy {
        savedState[GAME_STATE_KEY] ?: GameState()
    }

    val config: GameConfiguration by lazy {
        savedState.get<GameConfiguration>(GAME_CONFIGURATION_KEY) ?: throw IllegalArgumentException()
    }

    val words: ArrayList<String> by lazy {
        savedState.get<ArrayList<String>>(WORDS_KEY) ?: throw IllegalArgumentException()
    }

    private var gameListener: GameListener? = null

    fun setOnStateChangeListener(gameListener: GameListener) {
        this.gameListener = gameListener
    }

    fun startGame() {
        savedState[GAME_STATE_KEY] = game
        game.drawGuesses.add(Word(words.removeAt(0)))
        gameListener?.onStateChange(game.state)
    }

    fun defineDrawing() {
        game.drawGuesses.add(game.currentDrawing)

        // Add 1 to playerCount due to the first drawGuess being the word definition
        if (game.drawGuesses.size == config.playerCount + 1) {
            game.state = GameState.State.RESULTS
        } else {
            game.state = GameState.State.GUESSING
        }

        gameListener?.onStateChange(game.state)
    }

    fun defineGuess(word: String) {
        game.drawGuesses.add(Word(word))
        game.currentDrawing = Drawing()

        // Add 1 to playerCount due to the first drawGuess being the word definition
        if (game.drawGuesses.size == config.playerCount + 1) {
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

    fun getCurrentDrawing() : Drawing {
        return game.currentDrawing
    }

    fun getLastDrawGuess() : DrawGuess {
        return game.drawGuesses.last()
    }
}