package edu.isel.pdm.li51xd.g08.drag.game

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.firestore.ListenerRegistration
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.game.model.*
import edu.isel.pdm.li51xd.g08.drag.game.remote.Player
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.utils.runDelayed

const val IS_SCHEDULED_KEY = "Drag.IsScheduled"

class DrawViewModel(app: Application, private val savedState: SavedStateHandle) : AndroidViewModel(app) {

    val game: GameState by lazy {
        savedState[GAME_STATE_KEY] ?: GameState()
    }
    private val gameMode: Mode by lazy {
        Mode.valueOf(savedState.get<String>(GAME_MODE_KEY) ?: throw IllegalArgumentException())
    }
    val config: GameConfiguration by lazy {
        savedState.get<GameConfiguration>(GAME_CONFIGURATION_KEY) ?: throw IllegalArgumentException()
    }
    val words: ArrayList<String> by lazy {
        savedState.get<ArrayList<String>>(WORDS_KEY) ?: throw IllegalArgumentException()
    }


    val gameId: String by lazy {
        savedState.get<String>(GAME_ID_KEY) ?: throw IllegalArgumentException()
    }
    val player: Player by lazy {
        savedState.get<Player>(PLAYER_KEY) ?: throw IllegalArgumentException()
    }
    private var gameSubscription: ListenerRegistration? = null
    val currentDrawGuess: LiveData<DrawGuess> = MutableLiveData()

    private var isScheduled: Boolean = savedState[IS_SCHEDULED_KEY] ?: false

    fun scheduleWork(millis: Long, work: () -> Unit) {
        if (!isScheduled) {
            isScheduled = true
            savedState[IS_SCHEDULED_KEY] = isScheduled
            runDelayed(millis) {
                isScheduled = false
                work()
            }
        }
    }


    fun startGame() {
        if (gameMode == Mode.ONLINE) {
            getApplication<DragApplication>().repo.subscribeToGame(
                    gameId, player.id,
                    onSubscriptionError = { (currentDrawGuess as MutableLiveData<DrawGuess>).value = null },
                    onStateChange = { (currentDrawGuess as MutableLiveData<DrawGuess>).value = it }
            )
        }

        savedState[GAME_STATE_KEY] = game
        val startingWord = Word(words.removeAt(0))
        game.drawGuesses.add(startingWord)

        game.state = GameState.State.DRAWING
        (currentDrawGuess as MutableLiveData<DrawGuess>).value = startingWord
    }

    fun defineDrawing() {
        Log.v("fiche", "inside define drawing")
        game.drawGuesses.add(game.currentDrawing)

        // Add 1 to playerCount due to the first drawGuess being the word definition
        if (game.drawGuesses.size == config.playerCount + 1) {
            game.state = GameState.State.RESULTS
            (currentDrawGuess as MutableLiveData<DrawGuess>).value = null
        } else {
            game.state = GameState.State.GUESSING
            if (gameMode == Mode.OFFLINE) {
                (currentDrawGuess as MutableLiveData<DrawGuess>).value = game.currentDrawing
            }
        }
    }

    fun defineGuess(word: String) {
        Log.v("fiche", "inside define guess")
        val wordGuess = Word(word)
        game.drawGuesses.add(wordGuess)
        game.currentDrawing = Drawing()

        // Add 1 to playerCount due to the first drawGuess being the word definition
        if (game.drawGuesses.size == config.playerCount + 1) {
            game.state = GameState.State.RESULTS
            (currentDrawGuess as MutableLiveData<DrawGuess>).value = null
        } else {
            game.state = GameState.State.DRAWING
            if (gameMode == Mode.OFFLINE) {
                (currentDrawGuess as MutableLiveData<DrawGuess>).value = wordGuess
            }
        }
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