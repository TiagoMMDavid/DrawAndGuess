package edu.isel.pdm.li51xd.g08.drag.lobbies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.game.model.IS_SCHEDULED_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.LOBBY_INFO_KEY
import edu.isel.pdm.li51xd.g08.drag.game.remote.GameInfo
import edu.isel.pdm.li51xd.g08.drag.game.remote.LobbyInfo
import edu.isel.pdm.li51xd.g08.drag.game.remote.Player
import edu.isel.pdm.li51xd.g08.drag.utils.runDelayed

class DragLobbyViewModel(application: Application, private val savedState: SavedStateHandle) : AndroidViewModel(application) {

    val lobbyInfo: LiveData<LobbyInfo> by lazy { MutableLiveData(savedState.get(LOBBY_INFO_KEY)!!) }
    val gameInfo: LiveData<GameInfo> = MutableLiveData()

    private val app: DragApplication by lazy { getApplication<DragApplication>()}

    private val lobbySubscription = app.repo.subscribeToLobby(
        lobbyInfo.value!!.id,
        onSubscriptionError = { (lobbyInfo as MutableLiveData<LobbyInfo>).value = null },
        onStateChange = { (lobbyInfo as MutableLiveData<LobbyInfo>).value = it },
    )

    private val gameSubscription = app.repo.subscribeToGame(
        lobbyInfo.value!!.id,
        onSubscriptionError = { (gameInfo as MutableLiveData<GameInfo>).value = null },
        onStateChange = {
            clearSubscriptions()
            (gameInfo as MutableLiveData<GameInfo>).value = it
        }
    )

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

    fun clearSubscriptions() {
        lobbySubscription.remove()
        gameSubscription.remove()
    }

    fun exitLobby(player: Player) {
        app.repo.exitLobby(lobbyInfo.value!!.id, player)
    }
}