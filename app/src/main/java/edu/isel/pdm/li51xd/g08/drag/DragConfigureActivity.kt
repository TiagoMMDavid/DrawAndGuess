package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityConfigureBinding
import edu.isel.pdm.li51xd.g08.drag.model.*

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

        binding.startGameButton.setOnClickListener { startGame() }
    }

    private fun startGame() {
        startActivity(Intent(this, DragGameActivity::class.java).apply {
            putExtra(GAME_CONFIGURATION_KEY,
                    GameConfiguration(binding.playerCount.value, binding.roundCount.value, gameMode))
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