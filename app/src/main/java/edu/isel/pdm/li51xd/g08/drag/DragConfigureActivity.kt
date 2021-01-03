package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityConfigureBinding
import edu.isel.pdm.li51xd.g08.drag.game.DragGameActivity
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_CONFIGURATION_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_MODE_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.game.model.LOBBY_INFO_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.MAX_PLAYERS
import edu.isel.pdm.li51xd.g08.drag.game.model.MAX_ROUNDS
import edu.isel.pdm.li51xd.g08.drag.game.model.MIN_PLAYERS
import edu.isel.pdm.li51xd.g08.drag.game.model.MIN_ROUNDS
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode
import edu.isel.pdm.li51xd.g08.drag.game.model.PLAYER_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.PLAYER_NAME_KEY
import edu.isel.pdm.li51xd.g08.drag.lobbies.DragLobbyActivity
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.utils.EditTextNoEnter

class DragConfigureActivity : AppCompatActivity() {
    private val binding: ActivityConfigureBinding by lazy { ActivityConfigureBinding.inflate(layoutInflater) }

    private val gameMode: Mode by lazy {
        Mode.valueOf(intent.getStringExtra(GAME_MODE_KEY) ?: throw IllegalArgumentException())
    }

    private val playerName: String by lazy {
        intent.getStringExtra(PLAYER_NAME_KEY) ?: throw IllegalArgumentException()
    }

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

        when (gameMode) {
            Mode.OFFLINE -> {
                binding.lobbyName.visibility = INVISIBLE
            }
            Mode.ONLINE -> {
                binding.lobbyName.addTextChangedListener(EditTextNoEnter())
            }
        }

        binding.startGameButton.setOnClickListener {
            if (gameMode == Mode.ONLINE && binding.lobbyName.text.isBlank()) {
                Toast.makeText(this, R.string.errorNoLobbyName, Toast.LENGTH_LONG).show()
            } else {
                binding.startGameButton.isEnabled = false
                binding.loadingLobby.visibility = VISIBLE
                val app = application as DragApplication
                app.repo.fetchRandomWords(binding.roundCount.value).observe(this) {
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
        when (gameMode) {
            Mode.OFFLINE -> {
                startActivity(Intent(this, DragGameActivity::class.java).apply {
                    putExtra(GAME_CONFIGURATION_KEY, config)
                    putExtra(GAME_MODE_KEY, Mode.OFFLINE.name)
                    putStringArrayListExtra(WORDS_KEY, words)
                })
                finish()
            }
            Mode.ONLINE -> {
                (application as DragApplication).repo.createLobby(binding.lobbyName.text.toString(), playerName, config,
                    { lobby, player ->
                        startActivity(Intent(this, DragLobbyActivity::class.java).apply {
                            putExtra(LOBBY_INFO_KEY, lobby)
                            putStringArrayListExtra(WORDS_KEY, words)
                            putExtra(PLAYER_KEY, player)
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