package edu.isel.pdm.li51xd.g08.drag.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.firestore.ListenerRegistration
import edu.isel.pdm.li51xd.g08.drag.CURR_DRAWGUESS_KEY
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.GAME_CONFIGURATION_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_INFO_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_MODE_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_STATE_KEY
import edu.isel.pdm.li51xd.g08.drag.IS_WORK_CANCELLED
import edu.isel.pdm.li51xd.g08.drag.PLAYER_KEY
import edu.isel.pdm.li51xd.g08.drag.PLAYER_LEFT_KEY
import edu.isel.pdm.li51xd.g08.drag.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.OFFLINE
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.ONLINE
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import edu.isel.pdm.li51xd.g08.drag.remote.model.GameInfo
import edu.isel.pdm.li51xd.g08.drag.utils.runDelayed
import edu.isel.pdm.li51xd.g08.drag.utils.toValueList

class DragResultsViewModel(app: Application, private val savedState: SavedStateHandle) : AndroidViewModel(app) {

    private val app: DragApplication by lazy { getApplication<DragApplication>()}

    val gameMode: Mode by lazy {
        Mode.valueOf(savedState.get<String>(GAME_MODE_KEY) ?: throw IllegalArgumentException())
    }
    val game: GameState by lazy {
        savedState[GAME_STATE_KEY] ?: GameState()
    }
    val config: GameConfiguration by lazy {
        savedState.get<GameConfiguration>(GAME_CONFIGURATION_KEY) ?: throw IllegalArgumentException()
    }
    val words: ArrayList<String> by lazy {
        savedState.get<ArrayList<String>>(WORDS_KEY) ?: throw IllegalArgumentException()
    }

    var player: Player = savedState.get<Player>(PLAYER_KEY)!!
    var gameInfo: GameInfo? = savedState.get<GameInfo>(GAME_INFO_KEY)
    var currentDrawGuesses: LiveData<List<DrawGuess>> =
        if (savedState.contains(CURR_DRAWGUESS_KEY)) {
            MutableLiveData(savedState.get(CURR_DRAWGUESS_KEY))
        } else {
            MutableLiveData()
        }
    var playerLeft: Boolean = savedState.get(PLAYER_LEFT_KEY) ?: false
        set(value) { savedState[PLAYER_LEFT_KEY] = value; field = value }

    private var gameSubscription: ListenerRegistration? = null

    private var isScheduled: Boolean = false
    private var isWorkCancelled: Boolean = savedState[IS_WORK_CANCELLED] ?: false
        set(value) { savedState[IS_WORK_CANCELLED] = value; field = value }

    fun scheduleWork(millis: Long, work: () -> Unit) {
        if (!isScheduled && !isWorkCancelled) {
            isScheduled = true
            runDelayed(millis) {
                if (!isWorkCancelled) work()
            }
        }
    }

    fun cancelWork() {
        isWorkCancelled = true
    }

    fun gatherResults(finishedGatheringListener: () -> Unit) {
        when(gameMode) {
            OFFLINE -> {
                val value = (currentDrawGuesses as MutableLiveData).value
                if (value == null) {
                    updateCurrentDrawGuesses {
                        finishedGatheringListener()
                    }
                }
            }
            ONLINE -> {
                gameSubscription = gameSubscription ?: app.repo.subscribeToGame(
                    gameInfo!!.id,
                    onSubscriptionError = {
                        savedState[CURR_DRAWGUESS_KEY] = null
                        (currentDrawGuesses as MutableLiveData<List<DrawGuess>>).value = null
                    }, onStateChange = {
                        val value = (currentDrawGuesses as MutableLiveData).value
                        if (it.players.size != config.playerCount) {
                            playerLeft = true
                        }
                        if (value == null) {
                            savedState[GAME_INFO_KEY] = it
                            this.gameInfo = it
                            updateCurrentDrawGuesses {
                                finishedGatheringListener()
                            }
                        }
                    })
            }
        }
    }

    fun clearSubscription() {
        gameSubscription?.remove()
    }

    fun getPlayers() : List<Player> {
        return when(gameMode) {
            OFFLINE -> listOf(player)
            ONLINE -> gameInfo!!.players.toValueList().sortedBy { it.idx }
        }
    }

    fun getNextRoundId(): String {
        var gameId = gameInfo!!.id
        if (game.currRound > 1) {
            gameId = gameId.substring(0, gameId.lastIndex - 1)
        }
        return "${gameId}-${game.currRound + 1}"
    }

    fun createNextRound(hostId: String) {
        if (hostId == this.player.id) {
            app.repo.createGame(getNextRoundId(), gameInfo!!.players.toValueList().sortedBy { it.idx })
        }
    }

    fun deleteRound() {
        if (gameMode == ONLINE) {
            app.repo.deleteGame(gameInfo!!.id)
        }
    }

    fun deleteNextRound() {
        if (gameMode == ONLINE) {
            app.repo.deleteGame(getNextRoundId())
        }
    }

    fun exitGame() {
        if (gameMode == ONLINE) {
            app.repo.exitGame(gameInfo!!.id, gameInfo!!.players.getValue(player.id))
        }
    }

    fun updateCurrentDrawGuesses(playerId: String? = null, finishedGatheringListener: (() -> Unit)? = null) {
        when(gameMode) {
            OFFLINE -> {
                finishedGatheringListener?.invoke()
                savedState[CURR_DRAWGUESS_KEY] = player.book
                (currentDrawGuesses as MutableLiveData).value = player.book
            }
            ONLINE -> {
                if (playerId != null) {
                    val book = gameInfo!!.players[playerId]?.book
                    savedState[CURR_DRAWGUESS_KEY] = book
                    (currentDrawGuesses as MutableLiveData).value = book
                } else {
                    val players = gameInfo!!.players.toValueList()
                    for (player in players) {
                        if (player.book.size != config.playerCount + 1) {
                            return
                        }
                    }
                    finishedGatheringListener?.invoke()
                }
            }
        }
    }
}