package edu.isel.pdm.li51xd.g08.drag.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.firestore.ListenerRegistration
import edu.isel.pdm.li51xd.g08.drag.CURR_BOOK_OWNER_KEY
import edu.isel.pdm.li51xd.g08.drag.CURR_DRAWGUESS_KEY
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.GAME_CONFIGURATION_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_INFO_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_MODE_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_STATE_KEY
import edu.isel.pdm.li51xd.g08.drag.IS_WORK_CANCELLED
import edu.isel.pdm.li51xd.g08.drag.PENDING_DRAWGUESS_KEY
import edu.isel.pdm.li51xd.g08.drag.PLAYER_KEY
import edu.isel.pdm.li51xd.g08.drag.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import edu.isel.pdm.li51xd.g08.drag.game.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.OFFLINE
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.ONLINE
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.valueOf
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import edu.isel.pdm.li51xd.g08.drag.game.model.Point
import edu.isel.pdm.li51xd.g08.drag.game.model.Vector
import edu.isel.pdm.li51xd.g08.drag.game.model.Word
import edu.isel.pdm.li51xd.g08.drag.remote.model.GameInfo
import edu.isel.pdm.li51xd.g08.drag.remote.model.PlayerDrawGuess
import edu.isel.pdm.li51xd.g08.drag.utils.runDelayed
import edu.isel.pdm.li51xd.g08.drag.utils.toValueList

class DragGameViewModel(app: Application, private val savedState: SavedStateHandle) : AndroidViewModel(app) {

    private val app: DragApplication by lazy { getApplication<DragApplication>()}

    val game: GameState by lazy {
        savedState[GAME_STATE_KEY] ?: GameState()
    }
    val gameMode: Mode by lazy {
        valueOf(savedState.get<String>(GAME_MODE_KEY) ?: throw IllegalArgumentException())
    }
    val config: GameConfiguration by lazy {
        savedState.get<GameConfiguration>(GAME_CONFIGURATION_KEY) ?: throw IllegalArgumentException()
    }
    val words: ArrayList<String> by lazy {
        savedState.get<ArrayList<String>>(WORDS_KEY) ?: throw IllegalArgumentException()
    }

    val player: Player by lazy {
        savedState.get<Player>(PLAYER_KEY) ?: throw IllegalArgumentException()
    }
    val gameInfo: GameInfo by lazy {
        savedState.get<GameInfo>(GAME_INFO_KEY) ?: throw IllegalArgumentException()
    }

    val currentDrawGuess: LiveData<DrawGuess> by lazy {
        MutableLiveData(savedState[CURR_DRAWGUESS_KEY])
    }

    private val nextPlayerId: String by lazy {
        val players = gameInfo.players.toValueList().sortedBy { it.idx }
        var id: String? = null
        for(i in players.indices) {
            if (players[i].id == player.id) {
                id = if (i == players.size - 1) players[0].id
                else players[i + 1].id
                break
            }
        }
        id!!
    }

    private var drawGuessSubscription: ListenerRegistration? = null
    private var gameSubscription: ListenerRegistration? = null
    private var isScheduled: Boolean = false

    private var pendingDrawGuess: PlayerDrawGuess? = savedState.get(PENDING_DRAWGUESS_KEY)
        set(value) { savedState[PENDING_DRAWGUESS_KEY] = value; field = savedState[PENDING_DRAWGUESS_KEY] }
    private var currentBookOwnerId: String? = savedState.get(CURR_BOOK_OWNER_KEY)
        set(value) { savedState[CURR_BOOK_OWNER_KEY] = value; field = value }
    private var isWorkCancelled: Boolean = savedState[IS_WORK_CANCELLED] ?: false
        set(value) { savedState[IS_WORK_CANCELLED] = value; field = value }

    fun scheduleWork(millis: Long, work: () -> Unit) {
        if (!isScheduled && !isWorkCancelled) {
            isScheduled = true
            runDelayed(millis) {
                isScheduled = false
                if (!isWorkCancelled) {
                    work()
                }
            }
        }
    }

    fun cancelWork() {
        isWorkCancelled = true
    }

    fun clearSubscription() {
        drawGuessSubscription?.remove()
        gameSubscription?.remove()
    }

    private fun consumeDrawGuess(drawGuess: PlayerDrawGuess?) {
        if (drawGuess != null) {
            currentBookOwnerId = drawGuess.bookOwnerId
            (currentDrawGuess as MutableLiveData<DrawGuess>).value = drawGuess.drawGuess
            savedState[CURR_DRAWGUESS_KEY] = drawGuess.drawGuess
        }
    }

    fun subscribeIfNeeded() {
        if (gameMode == ONLINE) {
            if (drawGuessSubscription == null) {
                drawGuessSubscription =
                    app.repo.subscribeToDrawGuess(gameInfo.id, player.id,
                        onSubscriptionError = {
                            (currentDrawGuess as MutableLiveData<DrawGuess>).value = null
                        },
                        onStateChange = {
                            if (currentBookOwnerId == null) consumeDrawGuess(it)
                            else pendingDrawGuess = it
                        }
                    )
            }

            if (gameSubscription == null) {
                gameSubscription =
                    app.repo.subscribeToGame(gameInfo.id,
                        onSubscriptionError = {
                            (currentDrawGuess as MutableLiveData<DrawGuess>).value = null
                        },
                        onStateChange = {
                            if (it.players.size != config.playerCount) {
                                (currentDrawGuess as MutableLiveData<DrawGuess>).value = null
                            }
                        }
                    )
            }
        }
    }

    fun exitGame() {
        app.repo.exitGame(gameInfo.id, player)
    }

    fun startGame() {
        savedState[GAME_STATE_KEY] = game

        game.currentDrawing.clear()
        game.playCount = 0
        player.book.clear()

        val startingWord = Word(words.removeAt(0))

        if (gameMode == OFFLINE) {
            player.book.add(startingWord)
        }

        game.state = GameState.State.DRAWING
        if (gameMode == ONLINE) {
            subscribeIfNeeded()
            currentBookOwnerId = player.id
            app.repo.addDrawGuessToBook(gameInfo.id, currentBookOwnerId!!, startingWord)
        }

        (currentDrawGuess as MutableLiveData<DrawGuess>).value = startingWord
        savedState[CURR_DRAWGUESS_KEY] = startingWord
    }

    fun defineDrawing() {
        val currentDrawing = game.currentDrawing
        if (gameMode == OFFLINE) {
            player.book.add(currentDrawing.copy())
        }

        if (++game.playCount == config.playerCount) {
            game.state = GameState.State.RESULTS

            if (gameMode == ONLINE) {
                app.repo.addDrawGuessToBook(gameInfo.id, currentBookOwnerId!!, currentDrawing)
            }
            (currentDrawGuess as MutableLiveData<DrawGuess>).value = null
            savedState[CURR_DRAWGUESS_KEY] = null
        } else {
            game.state = GameState.State.GUESSING
            when(gameMode) {
                OFFLINE -> {
                    (currentDrawGuess as MutableLiveData<DrawGuess>).value = currentDrawing
                    savedState[CURR_DRAWGUESS_KEY] = currentDrawing
                }
                ONLINE -> {
                    app.repo.addDrawGuessToBook(gameInfo.id, currentBookOwnerId!!, currentDrawing)
                    app.repo.sendDrawGuess(gameInfo.id, nextPlayerId, currentBookOwnerId!!, currentDrawing)
                    currentBookOwnerId = null
                    consumeDrawGuess(pendingDrawGuess)
                    pendingDrawGuess = null
                }
            }
        }
    }

    fun defineGuess(word: String) {
        val wordGuess = Word(if (word.isBlank()) " " else word)
        if (gameMode == OFFLINE) {
            player.book.add(wordGuess)
        }
        game.currentDrawing.clear()

        if (++game.playCount == config.playerCount) {
            game.state = GameState.State.RESULTS
            if (gameMode == ONLINE) {
                app.repo.addDrawGuessToBook(gameInfo.id, currentBookOwnerId!!, wordGuess)
            }
            (currentDrawGuess as MutableLiveData<DrawGuess>).value = null
            savedState[CURR_DRAWGUESS_KEY] = null
        } else {
            game.state = GameState.State.DRAWING
            when(gameMode) {
                OFFLINE -> {
                    (currentDrawGuess as MutableLiveData<DrawGuess>).value = wordGuess
                    savedState[CURR_DRAWGUESS_KEY] = wordGuess
                }
                ONLINE -> {
                    app.repo.addDrawGuessToBook(gameInfo.id, currentBookOwnerId!!, wordGuess)
                    app.repo.sendDrawGuess(gameInfo.id, nextPlayerId, currentBookOwnerId!!, wordGuess)
                    currentBookOwnerId = null
                    consumeDrawGuess(pendingDrawGuess)
                    pendingDrawGuess = null
                }
            }
        }
    }

    fun addVectorToModel(x: Float, y: Float) {
        val vectors = game.currentDrawing.vectors
        vectors.add(Vector())
        vectors.last().points.add(Point(x, y))
    }

    fun addPointToModel(x: Float, y: Float) {
        if (game.currentDrawing.vectors.isEmpty()) {
            addVectorToModel(x, y)
        } else {
            game.currentDrawing.vectors.last().points.add(Point(x, y))
        }
    }

    fun getCurrentDrawing() : Drawing {
        return game.currentDrawing
    }
}