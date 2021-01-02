package edu.isel.pdm.li51xd.g08.drag.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.firestore.ListenerRegistration
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.game.model.*
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.*
import edu.isel.pdm.li51xd.g08.drag.game.remote.GameInfo
import edu.isel.pdm.li51xd.g08.drag.game.remote.Player
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.utils.runDelayed

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

    private val gameInfo: GameInfo by lazy {
        savedState.get<GameInfo>(GAME_INFO_KEY) ?: throw IllegalArgumentException()
    }
    val player: Player by lazy {
        savedState.get<Player>(PLAYER_KEY) ?: throw IllegalArgumentException()
    }
    private var gameSubscription: ListenerRegistration? = null
    val currentDrawGuess: LiveData<DrawGuess> = MutableLiveData()
    var currentBookOwnerId: String? = null
    var pendingRequest: (() -> Unit)? = null

    private val nextPlayerId: String by lazy {
        val players = gameInfo.players
        var id: String? = null
        for(i in 0 until players.size) {
            if (players[i].id == player.id) {
                id = if (i == players.size - 1) players[0].id
                else players[i + 1].id
                break
            }
        }
        id!!
    }

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

    fun clearSubscription() {
        gameSubscription?.remove()
    }

    fun startGame() {
        savedState[GAME_STATE_KEY] = game
        val startingWord = Word(words.removeAt(0))
        if (gameMode == OFFLINE) {
            game.drawGuesses.add(startingWord)
        }

        game.state = GameState.State.DRAWING
        if (gameMode == ONLINE) {
            gameSubscription = app.repo.subscribeToDrawGuess(
                gameInfo.id, player.id,
                onSubscriptionError = {
                    (currentDrawGuess as MutableLiveData<DrawGuess>).value = null
                },
                onStateChange = {
                    val request = {
                        currentBookOwnerId = it.bookOwnerId
                        (currentDrawGuess as MutableLiveData<DrawGuess>).value = it.drawGuess
                    }

                    if (currentBookOwnerId == null) request()
                    else pendingRequest = request
                })
            currentBookOwnerId = player.id
            app.repo.addDrawGuessToBook(gameInfo.id, currentBookOwnerId!!, startingWord)
        }
        (currentDrawGuess as MutableLiveData<DrawGuess>).value = startingWord
    }

    fun defineDrawing() {
        val currentDrawing = game.currentDrawing
        if (gameMode == OFFLINE) {
            game.drawGuesses.add(currentDrawing)
        }

        if (++game.playCount == config.playerCount) {
            game.state = GameState.State.RESULTS

            if (gameMode == ONLINE) {
                app.repo.addDrawGuessToBook(gameInfo.id, currentBookOwnerId!!, currentDrawing)
            }
            (currentDrawGuess as MutableLiveData<DrawGuess>).value = null
        } else {
            game.state = GameState.State.GUESSING
            when(gameMode) {
                OFFLINE -> {
                    (currentDrawGuess as MutableLiveData<DrawGuess>).value = currentDrawing
                }
                ONLINE -> {
                    app.repo.addDrawGuessToBook(gameInfo.id, currentBookOwnerId!!, currentDrawing)
                    app.repo.sendDrawGuess(gameInfo.id, nextPlayerId, currentBookOwnerId!!, currentDrawing)
                    currentBookOwnerId = null
                    pendingRequest?.invoke()
                    pendingRequest = null
                }
            }
        }
    }

    fun defineGuess(word: String) {
        val wordGuess = Word(word)
        if (gameMode == OFFLINE) {
            game.drawGuesses.add(wordGuess)
        }
        game.currentDrawing = Drawing()

        if (++game.playCount == config.playerCount) {
            game.state = GameState.State.RESULTS
            if (gameMode == ONLINE) {
                app.repo.addDrawGuessToBook(gameInfo.id, currentBookOwnerId!!, wordGuess)
            }
            (currentDrawGuess as MutableLiveData<DrawGuess>).value = null
        } else {
            game.state = GameState.State.DRAWING
            when(gameMode) {
                OFFLINE -> {
                    (currentDrawGuess as MutableLiveData<DrawGuess>).value = wordGuess
                }
                ONLINE -> {
                    app.repo.addDrawGuessToBook(gameInfo.id, currentBookOwnerId!!, wordGuess)
                    app.repo.sendDrawGuess(gameInfo.id, nextPlayerId, currentBookOwnerId!!, wordGuess)
                    currentBookOwnerId = null
                    pendingRequest?.invoke()
                    pendingRequest = null
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
}