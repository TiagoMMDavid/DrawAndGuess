package edu.isel.pdm.li51xd.g08.drag.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.GAME_MODE_KEY
import edu.isel.pdm.li51xd.g08.drag.PLAYER_NAME_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import edu.isel.pdm.li51xd.g08.drag.remote.model.LobbyInfo

class DragConfigureViewModel(app: Application, private val savedState: SavedStateHandle) : AndroidViewModel(app) {

    private val app: DragApplication by lazy { getApplication<DragApplication>()}

    val gameMode: Mode by lazy {
        Mode.valueOf(savedState.get(GAME_MODE_KEY) ?: throw IllegalArgumentException())
    }

    private val playerName: String by lazy {
        savedState.get<String>(PLAYER_NAME_KEY) ?: throw IllegalArgumentException()
    }

    fun fetchRandomWords(count: Int) = app.repo.fetchRandomWords(count)

    fun createLobby(lobbyName: String, config: GameConfiguration,
                    onSuccess: (LobbyInfo, Player) -> Unit, onError: (Exception) -> Unit) {
        app.repo.createLobby(lobbyName, playerName, config, onSuccess, onError)
    }
}