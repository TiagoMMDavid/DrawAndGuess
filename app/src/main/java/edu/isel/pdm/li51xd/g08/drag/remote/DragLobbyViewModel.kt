package edu.isel.pdm.li51xd.g08.drag.remote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import edu.isel.pdm.li51xd.g08.drag.*
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import edu.isel.pdm.li51xd.g08.drag.remote.model.GameInfo
import edu.isel.pdm.li51xd.g08.drag.remote.model.LobbyInfo
import edu.isel.pdm.li51xd.g08.drag.utils.runDelayed

class DragLobbyViewModel(application: Application, private val savedState: SavedStateHandle) : AndroidViewModel(application) {

    private val app: DragApplication by lazy { getApplication<DragApplication>()}

    val player: Player by lazy {
        savedState.get<Player>(PLAYER_KEY) ?: throw IllegalArgumentException()
    }
    val words: ArrayList<String> by lazy {
        savedState.get<ArrayList<String>>(WORDS_KEY) ?: throw IllegalArgumentException()
    }

    val lobbyInfo: LiveData<LobbyInfo> by lazy { MutableLiveData(savedState.get(LOBBY_INFO_KEY)!!) }
    val gameInfo: LiveData<GameInfo?> by lazy {
        val liveData = if (savedState.contains(GAME_INFO_KEY)) {
            MutableLiveData(savedState.get<GameInfo>(GAME_INFO_KEY))
        } else {
            MutableLiveData()
        }
        liveData
    }

    private val lobbySubscription = app.repo.subscribeToLobby(
        lobbyInfo.value!!.id, player,
        onSubscriptionError = { (lobbyInfo as MutableLiveData<LobbyInfo>).value = null },
        onStateChange = { (lobbyInfo as MutableLiveData<LobbyInfo>).value = it },
    )
    private val gameSubscription = app.repo.subscribeToGame(
        lobbyInfo.value!!.id, player.id,
        onSubscriptionError = {
            savedState[GAME_INFO_KEY] = null
            (gameInfo as MutableLiveData<GameInfo?>).value = null
        },
        onStateChange = {
            clearSubscriptions()
            savedState[GAME_INFO_KEY] = it
            (gameInfo as MutableLiveData<GameInfo?>).value = it
        }
    )

    private var isScheduled: Boolean = savedState[IS_SCHEDULED_KEY] ?: false

    fun scheduleWork(millis: Long, work: () -> Unit) {
        if (!isScheduled) {
            isScheduled = true
            savedState[IS_SCHEDULED_KEY] = isScheduled
            runDelayed(millis) {
                work()
            }
        }
    }

    fun clearSubscriptions() {
        lobbySubscription.remove()
        gameSubscription.remove()
    }

    fun exitLobby() {
        app.repo.exitLobby(lobbyInfo.value!!.id, player)
    }
}