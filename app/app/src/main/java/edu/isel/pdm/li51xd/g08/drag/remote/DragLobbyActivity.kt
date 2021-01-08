package edu.isel.pdm.li51xd.g08.drag.remote

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.isel.pdm.li51xd.g08.drag.*
import edu.isel.pdm.li51xd.g08.drag.R.string
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLobbyBinding
import edu.isel.pdm.li51xd.g08.drag.game.DragGameActivity
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import edu.isel.pdm.li51xd.g08.drag.remote.view.PlayerListAdapter
import edu.isel.pdm.li51xd.g08.drag.utils.toValueList

class DragLobbyActivity : AppCompatActivity() {

    private val binding: ActivityLobbyBinding by lazy { ActivityLobbyBinding.inflate(layoutInflater) }
    private val viewModel: DragLobbyViewModel by viewModels()

    private fun updateLobby(players: List<Player>?) {
        binding.playerNames.adapter = PlayerListAdapter(players ?: listOf(), viewModel.player)
    }

    private fun startGame() {
        viewModel.clearSubscriptions()
        val lobby = viewModel.lobbyInfo.value!!
        startActivity(Intent(this, DragGameActivity::class.java).apply {
            putExtra(GAME_MODE_KEY, Mode.ONLINE.name)
            putExtra(GAME_CONFIGURATION_KEY, lobby.gameConfig)

            putExtra(GAME_INFO_KEY, viewModel.gameInfo.value)
            putExtra(PLAYER_KEY, viewModel.player)
            putStringArrayListExtra(WORDS_KEY, viewModel.words)
        })
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.lobbyInfo.text = getString(string.lobbyWaiting)
        binding.playerNames.setHasFixedSize(true)
        binding.playerNames.layoutManager = LinearLayoutManager(this)

        viewModel.lobbyInfo.observe(this) {
            if (it == null) {
                binding.lobbyName.text = getString(string.error)
                binding.lobbyInfo.text = getString(string.errorJoinLobby)
            } else {
                binding.lobbyName.text = it.name
            }
            updateLobby(it?.players)
        }

        viewModel.gameInfo.observe(this) { gameInfo ->
            if (gameInfo == null) {
                binding.lobbyName.text = getString(string.error)
                binding.lobbyInfo.text = getString(string.errorJoinLobby)
            } else {
                updateLobby(gameInfo.players.toValueList().sortedBy { it.idx })

                viewModel.scheduleWork(viewModel.timeLeft) { startGame() }
                viewModel.startTimer {
                    binding.lobbyInfo.text = "${it / COUNTDOWN_INTERVAL}"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val gameInfo = viewModel.gameInfo.value
        updateLobby(gameInfo?.players?.toValueList()?.sortedBy { it.idx } ?: viewModel.lobbyInfo.value?.players)
    }

    override fun onBackPressed() {
        if (viewModel.gameInfo.value == null) {
            super.onBackPressed()
            viewModel.clearSubscriptions()
            viewModel.exitLobby()
        }
    }
}