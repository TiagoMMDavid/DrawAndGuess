package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityConfigureBinding
import edu.isel.pdm.li51xd.g08.drag.game.DragGameActivity
import edu.isel.pdm.li51xd.g08.drag.game.model.*
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
                val app = application as DragApplication
                app.repo.fetchRandomWords(binding.roundCount.value).observe(this) {
                    if (it.isSuccess) {
                        startGameOrLobby(ArrayList(it.getOrThrow()))
                    } else {
                        Toast.makeText(this, R.string.errorWordnik, Toast.LENGTH_LONG).show()
                        binding.startGameButton.isEnabled = true
                    }
                }
                binding.startGameButton.isEnabled = false
            }
        }
    }

    private fun startGameOrLobby(words: ArrayList<String>) {
        val config = GameConfiguration(binding.playerCount.value, binding.roundCount.value)
        when (gameMode) {
            Mode.OFFLINE -> {
                startActivity(Intent(this, DragGameActivity::class.java).apply {
                    putExtra(GAME_CONFIGURATION_KEY, config)
                    putStringArrayListExtra(WORDS_KEY, words)
                })
                finish()
            }
            Mode.ONLINE -> {
                (application as DragApplication).repo.createLobby(binding.lobbyName.text.toString(), playerName, config,
                    {
                        startActivity(Intent(this, DragLobbyActivity::class.java).apply {
                            putExtra(LOBBY_INFO_KEY, it)
                            putStringArrayListExtra(WORDS_KEY, words)
                        })
                        finish()
                    },
                    {
                        Toast.makeText(this, R.string.errorCreateLobby, Toast.LENGTH_LONG).show()
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