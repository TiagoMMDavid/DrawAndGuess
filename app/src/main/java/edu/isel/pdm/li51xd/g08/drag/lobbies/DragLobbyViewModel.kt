package edu.isel.pdm.li51xd.g08.drag.lobbies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.game.model.Player

class DragLobbyViewModel(private val lobby: LobbyInfo, app: Application) : AndroidViewModel(app) {
    private val lobbySubscription = getApplication<DragApplication>().repo.subscribeToLobby(
        lobby.id,
        onSubscriptionError = { TODO() },
        onStateChange = { (lobbyInfo as MutableLiveData<LobbyInfo>).value = it }
    )

    private val app: DragApplication by lazy { getApplication<DragApplication>()}

    val lobbyInfo: LiveData<LobbyInfo> = MutableLiveData(lobby)

    override fun onCleared() {
        super.onCleared()
        lobbySubscription.remove()
    }

    fun exitLobby(player: Player) {
        app.repo.exitLobby(lobby.id, player)
    }
}