package edu.isel.pdm.li51xd.g08.drag.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.firestore.ListenerRegistration
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.R
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_CONFIGURATION_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_INFO_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_MODE_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_STATE_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState
import edu.isel.pdm.li51xd.g08.drag.game.model.IS_SCHEDULED_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.OFFLINE
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.ONLINE
import edu.isel.pdm.li51xd.g08.drag.game.model.PLAYER_KEY
import edu.isel.pdm.li51xd.g08.drag.game.remote.GameInfo
import edu.isel.pdm.li51xd.g08.drag.game.remote.Player
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.utils.runDelayed

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

    var gameInfo: GameInfo = savedState.get<GameInfo>(GAME_INFO_KEY) ?: throw IllegalArgumentException()
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

    fun init() {
        when(gameMode) {
            OFFLINE -> {
                (currentDrawGuesses as MutableLiveData).value = game.drawGuesses
            }
            ONLINE -> {
                gameSubscription = gameSubscription ?: app.repo.subscribeToGame(
                    gameInfo.id,
                    onSubscriptionError = {
                        (currentDrawGuesses as MutableLiveData<List<DrawGuess>>).value = null
                    }, onStateChange = {
                        val value = (currentDrawGuesses as MutableLiveData).value
                        if (value.isNullOrEmpty()) {
                            this.gameInfo = it
                            savedState[GAME_INFO_KEY] = it
                            updateCurrentDrawGuesses()
                        }
                    })
            }
        }
    }

    fun clearSubscription() {
        gameSubscription?.remove()
    }

    fun exitGame() {
        if (gameMode == ONLINE) {
            app.repo.exitGame(gameInfo.id, player!!)
        }
    }

    fun getPlayers() : List<Player> {
        when(gameMode) {
            OFFLINE -> return listOf(Player(app.resources.getString(R.string.localPlayer)))
            ONLINE -> return gameInfo.players
        }
    }

    fun updateCurrentDrawGuesses(playerId: String? = null) {
        if (gameMode != ONLINE) throw IllegalStateException()

        for(i in 0 until gameInfo.players.size) {
            val player = gameInfo.players[i]
            if (player.book.size != config.playerCount + 1) {
                return
            }
            if (player.id == this.player!!.id) {
                this.player = player
            }
            if (player.id == playerId) {
                (currentDrawGuesses as MutableLiveData).value = player.book
                return
            }
        }
        (currentDrawGuesses as MutableLiveData).value = listOf()
    }
}