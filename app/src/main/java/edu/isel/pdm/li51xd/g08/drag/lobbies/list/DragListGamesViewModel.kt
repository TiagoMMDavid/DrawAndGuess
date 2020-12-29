package edu.isel.pdm.li51xd.g08.drag.lobbies.list

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.R
import edu.isel.pdm.li51xd.g08.drag.game.remote.Player
import edu.isel.pdm.li51xd.g08.drag.game.remote.LobbyInfo

class DragListGamesViewModel(app: Application) : AndroidViewModel(app) {

    private val app: DragApplication by lazy { getApplication<DragApplication>()}

    val lobbies: LiveData<List<LobbyInfo>> = MutableLiveData()
    val joinedLobby: LiveData<LobbyInfo> = MutableLiveData()
    var words: ArrayList<String>? = null
    var player: Player? = null

    fun fetchLobbies() {
        (joinedLobby as MutableLiveData<LobbyInfo>).value = null
        app.repo.fetchLobbies(
            {
                (lobbies as MutableLiveData<List<LobbyInfo>>).value = it
            },
            {
                Toast.makeText(app, R.string.errorGetLobbies, Toast.LENGTH_LONG).show()
            }
        )
    }

    fun tryJoinLobby(id: String, playerName: String, roundCount: Int) {
        if (words == null || words!!.size < roundCount) {
            app.repo.fetchRandomWords(roundCount).observeForever {
                if (it.isSuccess) {
                    words = ArrayList(it.getOrThrow())
                    joinLobby(id, playerName)
                } else {
                    Toast.makeText(app, R.string.errorWordnik, Toast.LENGTH_LONG).show()
                    (joinedLobby as MutableLiveData<LobbyInfo>).value = null
                }
            }
        } else {
            joinLobby(id, playerName)
        }
    }

    private fun joinLobby(id: String, playerName: String) {
        app.repo.tryJoinLobby(id, playerName,
            { lobby, player ->
                this.player = player
                (joinedLobby as MutableLiveData<LobbyInfo>).value = lobby
            },
            {
                Toast.makeText(app, R.string.errorJoinLobby, Toast.LENGTH_LONG).show()
                (joinedLobby as MutableLiveData<LobbyInfo>).value = null
            })
    }
}