package edu.isel.pdm.li51xd.g08.drag.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.firestore.ListenerRegistration
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import edu.isel.pdm.li51xd.g08.drag.game.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_CONFIGURATION_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_ID_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_MODE_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_STATE_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.OFFLINE
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.ONLINE
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.valueOf
import edu.isel.pdm.li51xd.g08.drag.game.model.PLAYER_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.Point
import edu.isel.pdm.li51xd.g08.drag.game.model.Vector
import edu.isel.pdm.li51xd.g08.drag.game.model.Word
import edu.isel.pdm.li51xd.g08.drag.game.remote.Player
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.utils.runDelayed

const val IS_SCHEDULED_KEY = "Drag.IsScheduled"

class DrawViewModel(app: Application, private val savedState: SavedStateHandle) : AndroidViewModel(app) {

    val game: GameState by lazy {
        savedState[GAME_STATE_KEY] ?: GameState()
    }
    private val gameMode: Mode by lazy {
        valueOf(savedState.get<String>(GAME_MODE_KEY) ?: throw IllegalArgumentException())
    }
    val config: GameConfiguration by lazy {
        savedState.get<GameConfiguration>(GAME_CONFIGURATION_KEY) ?: throw IllegalArgumentException()
    }
    val words: ArrayList<String> by lazy {
        savedState.get<ArrayList<String>>(WORDS_KEY) ?: throw IllegalArgumentException()
    }
    private val app: DragApplication by lazy { getApplication<DragApplication>()}


    private val gameId: String by lazy {
        savedState.get<String>(GAME_ID_KEY) ?: throw IllegalArgumentException()
    }
    val player: Player by lazy {
        savedState.get<Player>(PLAYER_KEY) ?: throw IllegalArgumentException()
    }
    private var gameSubscription: ListenerRegistration? = null
    val currentDrawGuess: LiveData<DrawGuess> = MutableLiveData()
    var currentBookOwnerId: String? = null
    var pendingRequest: (() -> Unit)? = null

    private var isScheduled: Boolean = savedState[IS_SCHEDULED_KEY] ?: false

    fun clearSubscription() {
        gameSubscription?.remove()
    }

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
        savedState[GAME_STATE_KEY] = game
        val startingWord = Word(words.removeAt(0))
        game.drawGuesses.add(startingWord)
        game.state = GameState.State.DRAWING

        if (gameMode == ONLINE) {
            gameSubscription = app.repo.subscribeToGame(
                gameId, player.id,
                onSubscriptionError = { (currentDrawGuess as MutableLiveData<DrawGuess>).value = null },
                onStateChange = {
                    val request = {
                        currentBookOwnerId = it.bookOwnerId
                        (currentDrawGuess as MutableLiveData<DrawGuess>).value = it.drawGuess
                    }
                    if (currentBookOwnerId == null) request()
                    else pendingRequest = request
                })
            currentBookOwnerId = player.id
            app.repo.addDrawGuessToBook(gameId, currentBookOwnerId!!, startingWord)
        }
        (currentDrawGuess as MutableLiveData<DrawGuess>).value = startingWord
    }

    fun defineDrawing() {
        val currentDrawing = game.currentDrawing

        game.drawGuesses.add(currentDrawing)

        // Add 1 to playerCount due to the first drawGuess being the word definition
        if (game.drawGuesses.size == config.playerCount + 1) {
            game.state = GameState.State.RESULTS

            if (gameMode == ONLINE) {
                app.repo.addDrawGuessToBook(gameId, currentBookOwnerId!!, currentDrawing)
            }
            (currentDrawGuess as MutableLiveData<DrawGuess>).value = null
        } else {
            game.state = GameState.State.GUESSING
            when(gameMode) {
                OFFLINE -> {
                    (currentDrawGuess as MutableLiveData<DrawGuess>).value = currentDrawing
                }
                ONLINE -> {
                    app.repo.addDrawGuessToBook(gameId, currentBookOwnerId!!, currentDrawing)
                    app.repo.sendDrawGuess(gameId, player.id, currentBookOwnerId!!, currentDrawing)
                    currentBookOwnerId = null
                    pendingRequest?.invoke()
                    pendingRequest = null
                }
            }
        }
    }

    fun defineGuess(word: String) {
        val wordGuess = Word(word)
        game.drawGuesses.add(wordGuess)
        game.currentDrawing = Drawing()

        // Add 1 to playerCount due to the first drawGuess being the word definition
        if (game.drawGuesses.size == config.playerCount + 1) {
            game.state = GameState.State.RESULTS
            if (gameMode == ONLINE) {
                app.repo.addDrawGuessToBook(gameId, currentBookOwnerId!!, wordGuess)
            }
            (currentDrawGuess as MutableLiveData<DrawGuess>).value = null
        } else {
            game.state = GameState.State.DRAWING
            when(gameMode) {
                OFFLINE -> {
                    (currentDrawGuess as MutableLiveData<DrawGuess>).value = wordGuess
                }
                ONLINE -> {
                    app.repo.addDrawGuessToBook(gameId, currentBookOwnerId!!, wordGuess)
                    app.repo.sendDrawGuess(gameId, player.id, currentBookOwnerId!!, wordGuess)
                    currentBookOwnerId = null
                    pendingRequest?.invoke()
                }
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