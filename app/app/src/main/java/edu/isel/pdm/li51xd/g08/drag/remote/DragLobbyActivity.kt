package edu.isel.pdm.li51xd.g08.drag.remote

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.isel.pdm.li51xd.g08.drag.COUNTDOWN_INTERVAL
import edu.isel.pdm.li51xd.g08.drag.COUNTDOWN_TIME_LEFT_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_CONFIGURATION_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_INFO_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_MODE_KEY
import edu.isel.pdm.li51xd.g08.drag.LOBBY_COUNTDOWN_TIME
import edu.isel.pdm.li51xd.g08.drag.PLAYER_KEY
import edu.isel.pdm.li51xd.g08.drag.R.string
import edu.isel.pdm.li51xd.g08.drag.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLobbyBinding
import edu.isel.pdm.li51xd.g08.drag.game.DragGameActivity
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import edu.isel.pdm.li51xd.g08.drag.remote.view.PlayerListAdapter
import edu.isel.pdm.li51xd.g08.drag.utils.CountDownTimerAdapter
import edu.isel.pdm.li51xd.g08.drag.utils.toValueList

class DragLobbyActivity : AppCompatActivity() {

    private val binding: ActivityLobbyBinding by lazy { ActivityLobbyBinding.inflate(layoutInflater) }
    private val viewModel: DragLobbyViewModel by viewModels()

    private var timer: CountDownTimer? = null
    private var timeLeft: Long = LOBBY_COUNTDOWN_TIME

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

        timeLeft = savedInstanceState?.getLong(COUNTDOWN_TIME_LEFT_KEY, LOBBY_COUNTDOWN_TIME) ?: LOBBY_COUNTDOWN_TIME

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

                viewModel.scheduleWork(timeLeft) { startGame() }
                timer = CountDownTimerAdapter(timeLeft, COUNTDOWN_INTERVAL) { millisUntilFinished ->
                    timeLeft = millisUntilFinished
                    binding.lobbyInfo.text = "${millisUntilFinished / COUNTDOWN_INTERVAL}"
                }
                timer?.start()
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(COUNTDOWN_TIME_LEFT_KEY, timeLeft)
        timer?.cancel()
    }
}