package edu.isel.pdm.li51xd.g08.drag.lobbies

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.isel.pdm.li51xd.g08.drag.R.string
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLobbyBinding
import edu.isel.pdm.li51xd.g08.drag.game.DragGameActivity
import edu.isel.pdm.li51xd.g08.drag.game.model.*
import edu.isel.pdm.li51xd.g08.drag.game.remote.LobbyInfo
import edu.isel.pdm.li51xd.g08.drag.game.remote.Player
import edu.isel.pdm.li51xd.g08.drag.lobbies.view.PlayerListAdapter
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.utils.CountDownTimerAdapter
import edu.isel.pdm.li51xd.g08.drag.utils.toValueList

private const val COUNTDOWN_TIME = 5000L

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

    private val viewModel: DragLobbyViewModel by viewModels()

    private var timer: CountDownTimer? = null
    private var timeLeft: Long = COUNTDOWN_TIME

    private fun updateLobby(players: List<Player>?) {
        binding.playerNames.adapter = PlayerListAdapter(players ?: listOf(), player)
    }

    private fun startGame() {
        viewModel.clearSubscriptions()
        val lobby = viewModel.lobbyInfo.value!!
        startActivity(Intent(this, DragGameActivity::class.java).apply {
            putExtra(GAME_CONFIGURATION_KEY, lobby.gameConfig)
            putExtra(GAME_INFO_KEY, viewModel.gameInfo.value)
            putExtra(GAME_MODE_KEY, Mode.ONLINE.name)

            putExtra(PLAYER_KEY, player)
            putStringArrayListExtra(WORDS_KEY, words)
        })
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        timeLeft = savedInstanceState?.getLong(COUNTDOWN_TIME_LEFT_KEY, COUNTDOWN_TIME) ?: COUNTDOWN_TIME

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

        viewModel.gameInfo.observe(this) { gameInfo ->
            updateLobby(gameInfo.players.toValueList().sortedBy { it.idx })

            viewModel.scheduleWork(COUNTDOWN_TIME) { startGame() }
            timer = CountDownTimerAdapter(timeLeft, COUNTDOWN_INTERVAL) { millisUntilFinished ->
                timeLeft = millisUntilFinished
                binding.lobbyInfo.text = "${millisUntilFinished / COUNTDOWN_INTERVAL}"
            }
            timer?.start()
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
            viewModel.exitLobby(player)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(COUNTDOWN_TIME_LEFT_KEY, timeLeft)
        timer?.cancel()
    }
}