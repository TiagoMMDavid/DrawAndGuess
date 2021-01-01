package edu.isel.pdm.li51xd.g08.drag.lobbies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.game.model.COUNTDOWN_STARTED_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.LOBBY_INFO_KEY
import edu.isel.pdm.li51xd.g08.drag.game.remote.GameInfo
import edu.isel.pdm.li51xd.g08.drag.game.remote.LobbyInfo
import edu.isel.pdm.li51xd.g08.drag.game.remote.Player

class DragLobbyViewModel(app: Application, private val savedState: SavedStateHandle) : AndroidViewModel(app) {

    val lobbyInfo: LiveData<LobbyInfo> by lazy { MutableLiveData(savedState.get(LOBBY_INFO_KEY)!!) }
    val gameInfo: LiveData<GameInfo> = MutableLiveData()

    private val app: DragApplication by lazy { getApplication<DragApplication>()}

    private val lobbySubscription = getApplication<DragApplication>().repo.subscribeToLobby(
        lobbyInfo.value!!.id,
        onSubscriptionError = { (lobbyInfo as MutableLiveData<LobbyInfo>).value = null },
        onStateChange = { (lobbyInfo as MutableLiveData<LobbyInfo>).value = it },
    )

    private val gameSubscription = getApplication<DragApplication>().repo.subscribeToGame(
        lobbyInfo.value!!.id,
        onSubscriptionError = { (gameInfo as MutableLiveData<GameInfo>).value = null },
        onStateChange = {
            (gameInfo as MutableLiveData<GameInfo>).value = it
        }
    )

    var countdownStarted = savedState.get<Boolean>(COUNTDOWN_STARTED_KEY) ?: false
    set(value) {
        field = value
        savedState[COUNTDOWN_STARTED_KEY] = true
    }

    fun clearSubscriptions() {
        lobbySubscription.remove()
        gameSubscription.remove()
    }

    fun exitLobby(player: Player) {
        app.repo.exitLobby(lobbyInfo.value!!.id, player)
    }
}