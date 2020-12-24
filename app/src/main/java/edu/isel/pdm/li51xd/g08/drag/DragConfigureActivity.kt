package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityConfigureBinding
import edu.isel.pdm.li51xd.g08.drag.game.DragGameActivity
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_CONFIGURATION_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_MODE_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.game.model.MAX_PLAYERS
import edu.isel.pdm.li51xd.g08.drag.game.model.MAX_ROUNDS
import edu.isel.pdm.li51xd.g08.drag.game.model.MIN_PLAYERS
import edu.isel.pdm.li51xd.g08.drag.game.model.MIN_ROUNDS
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY

class DragConfigureActivity : AppCompatActivity() {
    private val binding: ActivityConfigureBinding by lazy { ActivityConfigureBinding.inflate(layoutInflater) }

    private val gameMode: GameConfiguration.Mode by lazy {
        GameConfiguration.Mode.valueOf(intent.getStringExtra(GAME_MODE_KEY) ?: throw IllegalArgumentException())
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

        binding.startGameButton.setOnClickListener {
            val app = application as DragApplication
            app.repo.fetchWords(binding.roundCount.value).observe(this) {
                if (it.isSuccess) {
                    startGame(ArrayList(it.getOrThrow()))
                } else {
                    Toast.makeText(
                        this,
                        resources.getString(R.string.errorWordnik),
                        Toast.LENGTH_LONG
                    ).show()
                    binding.startGameButton.isEnabled = true
                }
            }
            binding.startGameButton.isEnabled = false
        }
    }

    private fun startGame(words: ArrayList<String>) {
        startActivity(Intent(this, DragGameActivity::class.java).apply {
            putExtra(GAME_CONFIGURATION_KEY,
                    GameConfiguration(binding.playerCount.value, binding.roundCount.value, gameMode))
            putStringArrayListExtra(WORDS_KEY, words)
        })
        finish()
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