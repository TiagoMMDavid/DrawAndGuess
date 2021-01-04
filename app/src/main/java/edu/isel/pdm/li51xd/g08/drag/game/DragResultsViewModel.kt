package edu.isel.pdm.li51xd.g08.drag.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.firestore.ListenerRegistration
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.R
import edu.isel.pdm.li51xd.g08.drag.game.model.*
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.OFFLINE
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.ONLINE
import edu.isel.pdm.li51xd.g08.drag.game.remote.GameInfo
import edu.isel.pdm.li51xd.g08.drag.game.remote.Player
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.utils.runDelayed
import edu.isel.pdm.li51xd.g08.drag.utils.toValueList

class DragResultsViewModel(app: Application, private val savedState: SavedStateHandle) : AndroidViewModel(app) {
    val game: GameState by lazy {
        savedState[GAME_STATE_KEY] ?: GameState()
    }
    val gameMode: Mode by lazy {
        Mode.valueOf(savedState.get<String>(GAME_MODE_KEY) ?: throw IllegalArgumentException())
    }
    val config: GameConfiguration by lazy {
        savedState.get<GameConfiguration>(GAME_CONFIGURATION_KEY) ?: throw IllegalArgumentException()
    }
    val words: ArrayList<String> by lazy {
        savedState.get<ArrayList<String>>(WORDS_KEY) ?: throw IllegalArgumentException()
    }
    var player: Player? = savedState.get<Player>(PLAYER_KEY)

    var gameInfo: GameInfo? = savedState.get<GameInfo>(GAME_INFO_KEY)
    var currentDrawGuesses: LiveData<List<DrawGuess>> = MutableLiveData()

    private val app: DragApplication by lazy { getApplication<DragApplication>()}
    private var gameSubscription: ListenerRegistration? = null

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

    fun gatherResults(finishedGatheringListener: () -> Unit) {
        when(gameMode) {
            OFFLINE -> {
                updateCurrentDrawGuesses {
                    finishedGatheringListener()
                }
            }
            ONLINE -> {
                gameSubscription = gameSubscription ?: app.repo.subscribeToGame(
                    gameInfo!!.id,
                    onSubscriptionError = {
                        (currentDrawGuesses as MutableLiveData<List<DrawGuess>>).value = null
                    }, onStateChange = {
                        val value = (currentDrawGuesses as MutableLiveData).value
                        if (value == null) {
                            this.gameInfo = it
                            savedState[GAME_INFO_KEY] = it
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

    fun createNextRound(hostId: String) {
        if (hostId == this.player!!.id) {
            app.repo.createGame(getNextRoundString(), gameInfo!!.players.toValueList().sortedBy { it.idx }, {})
        }
    }

    fun getNextRoundString(): String {
        var gameId = gameInfo!!.id
        if (game.currRound > 1) {
            gameId = gameId.substring(0, gameId.lastIndex - 1)
        }
        return "${gameId}-${game.currRound + 1}"
    }

    fun deleteRound(hostId: String?) {
        if (hostId != null && hostId == player?.id) {
            app.repo.deleteGame(gameInfo!!.id)
        }
    }

    fun exitGame() {
        if (gameMode == ONLINE) {
            app.repo.exitGame(gameInfo!!.id, gameInfo!!.players.getValue(player!!.id))
        }
    }

    fun getPlayers() : List<Player> {
        when(gameMode) {
            OFFLINE -> return listOf(Player(name = app.resources.getString(R.string.localPlayer)))
            ONLINE -> return gameInfo!!.players.toValueList().sortedBy { it.idx }
        }
    }

    fun updateCurrentDrawGuesses(playerId: String? = null, finishedGatheringListener: (() -> Unit)? = null) {
        when(gameMode) {
            OFFLINE -> {
                finishedGatheringListener?.invoke()
                (currentDrawGuesses as MutableLiveData).value = game.drawGuesses
            }
            ONLINE -> {
                if (playerId != null) {
                    (currentDrawGuesses as MutableLiveData).value = gameInfo!!.players[playerId]?.book
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