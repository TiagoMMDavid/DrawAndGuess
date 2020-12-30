package edu.isel.pdm.li51xd.g08.drag.lobbies

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import edu.isel.pdm.li51xd.g08.drag.DragConfigureActivity
import edu.isel.pdm.li51xd.g08.drag.R.string
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLobbyBinding
import edu.isel.pdm.li51xd.g08.drag.game.DragGameActivity
import edu.isel.pdm.li51xd.g08.drag.game.model.*
import edu.isel.pdm.li51xd.g08.drag.game.remote.LobbyInfo
import edu.isel.pdm.li51xd.g08.drag.game.remote.Player
import edu.isel.pdm.li51xd.g08.drag.lobbies.view.PlayerListAdapter
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY

private const val COUNTDOWN_TIME = 5000L
private const val COUNTDOWN_INTERVAL = 1000L

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

    private fun updateLobby(players: List<Player>?) {
        binding.playerNames.adapter = PlayerListAdapter(players ?: listOf(), player)
    }

    private fun startGame() {
        val lobby = viewModel.lobbyInfo.value!!
        startActivity(Intent(this, DragGameActivity::class.java).apply {
            putExtra(GAME_CONFIGURATION_KEY, lobby.gameConfig)
            putExtra(GAME_ID_KEY, lobby.id)
            putExtra(GAME_MODE_KEY, Mode.ONLINE.name)

            putExtra(PLAYER_KEY, player)
            putStringArrayListExtra(WORDS_KEY, words)
        })
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.lobbyName.text = lobbyInfo.name
        binding.lobbyInfo.text = getString(string.lobbyWaiting)
        binding.playerNames.setHasFixedSize(true)
        binding.playerNames.layoutManager = LinearLayoutManager(this)

        viewModel.lobbyInfo.observe(this) {
            if (it == null) {
                binding.lobbyName.text = getString(string.error)
                binding.lobbyInfo.text = getString(string.errorJoinLobby)
            }
            updateLobby(it.players)
        }

        viewModel.gameInfo.observe(this) {
            updateLobby(it.players)
            val timer = object: CountDownTimer(COUNTDOWN_TIME, COUNTDOWN_INTERVAL) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.lobbyInfo.text = "${millisUntilFinished / COUNTDOWN_INTERVAL}"
                }

                override fun onFinish() {
                    startGame()
                }
            }
            timer.start()
        }
    }

    override fun onResume() {
        super.onResume()
        updateLobby(lobbyInfo.players)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.exitLobby(player)
    }
}