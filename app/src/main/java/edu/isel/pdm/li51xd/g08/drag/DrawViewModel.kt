package edu.isel.pdm.li51xd.g08.drag

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import edu.isel.pdm.li51xd.g08.drag.listeners.GameListener
import edu.isel.pdm.li51xd.g08.drag.model.DrawGuess
import edu.isel.pdm.li51xd.g08.drag.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.model.GameState
import edu.isel.pdm.li51xd.g08.drag.model.Point
import edu.isel.pdm.li51xd.g08.drag.model.Repository
import edu.isel.pdm.li51xd.g08.drag.model.Vector
import edu.isel.pdm.li51xd.g08.drag.model.Word

class DrawViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: Repository = getApplication<DragApplication>().repo

    private var gameListener: GameListener? = null

    fun setOnStateChangeListener(gameListener: GameListener) {
        this.gameListener = gameListener
    }

    fun startGame() {
        gameListener?.onStateChange(repo.game.state)
    }

    fun defineWord(word: String) {
        repo.game.drawGuesses.add(Word(word))

        repo.game.state = GameState.State.DRAWING
        gameListener?.onStateChange(repo.game.state)
    }

    fun defineDrawing() {
        repo.game.drawGuesses.add(repo.game.currentDrawing)

        if (repo.game.drawGuesses.size == repo.config.playerCount) {
            repo.game.state = GameState.State.RESULTS
        } else {
            repo.game.state = GameState.State.GUESSING
        }

        gameListener?.onStateChange(repo.game.state)

    }

    fun defineGuess(word: String) {
        repo.game.drawGuesses.add(Word(word))
        repo.game.currentDrawing = Drawing()

        if (repo.game.drawGuesses.size == repo.config.playerCount) {
            repo.game.state = GameState.State.RESULTS
        } else {
            repo.game.state = GameState.State.DRAWING
        }

        gameListener?.onStateChange(repo.game.state)
    }

    fun addVectorToModel(x: Float, y: Float) {
        val vectors = repo.game.currentDrawing.vectors
        vectors.add(Vector())
        vectors.last.points.add(Point(x, y))
    }

    fun addPointToModel(x: Float, y: Float) {
        repo.game.currentDrawing.vectors.last.points.add(Point(x, y))
    }

    fun getCurrentDrawing() : Drawing {
        return repo.game.currentDrawing
    }

    fun getLastDrawGuess() : DrawGuess {
        return repo.game.drawGuesses.last()
    }
}