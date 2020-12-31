package edu.isel.pdm.li51xd.g08.drag.lobbies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.game.remote.GameInfo
import edu.isel.pdm.li51xd.g08.drag.game.remote.LobbyInfo
import edu.isel.pdm.li51xd.g08.drag.game.remote.Player

class DragLobbyViewModel(private val lobby: LobbyInfo, app: Application) : AndroidViewModel(app) {
    private val lobbySubscription = getApplication<DragApplication>().repo.subscribeToLobby(
        lobby.id,
        onSubscriptionError = { (lobbyInfo as MutableLiveData<LobbyInfo>).value = null },
        onStateChange = { (lobbyInfo as MutableLiveData<LobbyInfo>).value = it },
    )

    private val gameSubscription = getApplication<DragApplication>().repo.subscribeToGame(
        lobby.id,
        onSubscriptionError = { (gameInfo as MutableLiveData<GameInfo>).value = null },
        onStateChange = {
            (gameInfo as MutableLiveData<GameInfo>).value = it
        }
    )

    private val app: DragApplication by lazy { getApplication<DragApplication>()}

    val lobbyInfo: LiveData<LobbyInfo> = MutableLiveData(lobby)
    val gameInfo: LiveData<GameInfo> = MutableLiveData()

    fun clearSubscriptions() {
        lobbySubscription.remove()
        gameSubscription.remove()
    }

    fun exitLobby(player: Player) {
        app.repo.exitLobby(lobby.id, player)
    }
}