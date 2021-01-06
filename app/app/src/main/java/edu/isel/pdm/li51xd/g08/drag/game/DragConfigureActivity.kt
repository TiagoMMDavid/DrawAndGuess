package edu.isel.pdm.li51xd.g08.drag.game

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.GAME_CONFIGURATION_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_MODE_KEY
import edu.isel.pdm.li51xd.g08.drag.LOBBY_INFO_KEY
import edu.isel.pdm.li51xd.g08.drag.MAX_PLAYERS
import edu.isel.pdm.li51xd.g08.drag.MAX_ROUNDS
import edu.isel.pdm.li51xd.g08.drag.MIN_PLAYERS
import edu.isel.pdm.li51xd.g08.drag.MIN_ROUNDS
import edu.isel.pdm.li51xd.g08.drag.PLAYER_KEY
import edu.isel.pdm.li51xd.g08.drag.R
import edu.isel.pdm.li51xd.g08.drag.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityConfigureBinding
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import edu.isel.pdm.li51xd.g08.drag.remote.DragLobbyActivity
import edu.isel.pdm.li51xd.g08.drag.utils.EditTextNoEnter

class DragConfigureActivity : AppCompatActivity() {

    private val binding: ActivityConfigureBinding by lazy { ActivityConfigureBinding.inflate(layoutInflater) }
    private val viewModel: DragConfigureViewModel by viewModels()

    private fun setUpLayout(savedInstanceState: Bundle?) {
        binding.playerCount.maxValue = MAX_PLAYERS
        binding.playerCount.minValue = MIN_PLAYERS
        binding.roundCount.maxValue = MAX_ROUNDS
        binding.roundCount.minValue = MIN_ROUNDS

        if (savedInstanceState != null) {
            val config = savedInstanceState.getParcelable<GameConfiguration>(GAME_CONFIGURATION_KEY) ?: throw IllegalStateException()
            binding.playerCount.value = config.playerCount
            binding.roundCount.value = config.roundCount
        }

        when (viewModel.gameMode) {
            Mode.OFFLINE -> {
                binding.lobbyName.visibility = INVISIBLE
            }
            Mode.ONLINE -> {
                binding.lobbyName.addTextChangedListener(EditTextNoEnter())
            }
        }

        binding.startGameButton.setOnClickListener {
            if (viewModel.gameMode == Mode.ONLINE && binding.lobbyName.text.isBlank()) {
                Toast.makeText(this, R.string.errorNoLobbyName, Toast.LENGTH_LONG).show()
            } else {
                binding.startGameButton.isEnabled = false
                binding.loadingLobby.visibility = VISIBLE
                viewModel.fetchRandomWords(binding.roundCount.value).observe(this) {
                    if (it.isSuccess) {
                        startGameOrLobby(ArrayList(it.getOrThrow()))
                    } else {
                        Toast.makeText(this, R.string.errorWordnik, Toast.LENGTH_LONG).show()
                        binding.startGameButton.isEnabled = true
                        binding.loadingLobby.visibility = GONE
                    }
                }
            }
        }
    }

    private fun startGameOrLobby(words: ArrayList<String>) {
        val config = GameConfiguration(binding.playerCount.value, binding.roundCount.value)
        when (viewModel.gameMode) {
            Mode.OFFLINE -> {
                startActivity(Intent(this, DragGameActivity::class.java).apply {
                    putExtra(GAME_MODE_KEY, Mode.OFFLINE.name)
                    putExtra(GAME_CONFIGURATION_KEY, config)
                    putExtra(PLAYER_KEY, Player(name = getString(R.string.localPlayer)))
                    putStringArrayListExtra(WORDS_KEY, words)
                })
                finish()
            }
            Mode.ONLINE -> {
                viewModel.createLobby(binding.lobbyName.text.toString(), config,
                    { lobby, player ->
                        startActivity(Intent(this, DragLobbyActivity::class.java).apply {
                            putExtra(LOBBY_INFO_KEY, lobby)
                            putExtra(PLAYER_KEY, player)
                            putStringArrayListExtra(WORDS_KEY, words)
                        })
                        finish()
                    },
                    {
                        Toast.makeText(this, R.string.errorCreateLobby, Toast.LENGTH_LONG).show()
                        binding.loadingLobby.visibility = GONE
                    })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpLayout(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(GAME_CONFIGURATION_KEY, GameConfiguration(binding.playerCount.value, binding.roundCount.value))
    }
}