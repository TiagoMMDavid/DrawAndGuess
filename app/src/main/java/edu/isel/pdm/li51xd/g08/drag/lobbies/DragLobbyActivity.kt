package edu.isel.pdm.li51xd.g08.drag.lobbies

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import edu.isel.pdm.li51xd.g08.drag.R.string
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLobbyBinding
import edu.isel.pdm.li51xd.g08.drag.game.model.LOBBY_INFO_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.PLAYER_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import edu.isel.pdm.li51xd.g08.drag.lobbies.view.PlayerListAdapter
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY

class DragLobbyActivity : AppCompatActivity() {

    private val binding: ActivityLobbyBinding by lazy { ActivityLobbyBinding.inflate(layoutInflater) }

    private val lobbyInfo: LobbyInfo by lazy {
        intent.getParcelableExtra<LobbyInfo>(LOBBY_INFO_KEY) ?: throw IllegalArgumentException()
    }

    private val words: ArrayList<String> by lazy {
        intent.getStringArrayListExtra(WORDS_KEY) ?: throw IllegalArgumentException()
    }

    private val player: Player by lazy {
        intent.getParcelableExtra<Player>(PLAYER_KEY) ?: throw IllegalArgumentException()
    }

    private val viewModel: DragLobbyViewModel by viewModels {
        @Suppress("UNCHECKED_CAST")
        object: ViewModelProvider.Factory {
            override fun <VM : ViewModel?> create(modelClass: Class<VM>): VM {
                return DragLobbyViewModel(lobbyInfo, application) as VM
            }
        }
    }

    private fun updateLobby(lobby: LobbyInfo) {
        binding.playerNames.adapter = PlayerListAdapter(lobby.players, player)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.lobbyName.text = lobbyInfo.name
        binding.lobbyInfo.text = getString(string.lobbyWaiting)
        binding.playerNames.setHasFixedSize(true)
        binding.playerNames.layoutManager = LinearLayoutManager(this)

        viewModel.lobbyInfo.observe(this) {
            updateLobby(it)
        }
    }

    override fun onStart() {
        super.onStart()
        updateLobby(lobbyInfo)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.exitLobby(player)
    }
}