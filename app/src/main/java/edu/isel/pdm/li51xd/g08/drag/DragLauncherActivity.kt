package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLauncherBinding
import edu.isel.pdm.li51xd.g08.drag.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.model.GameState
import edu.isel.pdm.li51xd.g08.drag.model.MAX_PLAYERS
import edu.isel.pdm.li51xd.g08.drag.model.MAX_ROUNDS
import edu.isel.pdm.li51xd.g08.drag.model.MIN_PLAYERS
import edu.isel.pdm.li51xd.g08.drag.model.MIN_ROUNDS

const val GAME_CONFIGURATION_KEY = "DRAG.GameConfiguration"

class DragLauncher : AppCompatActivity() {
    private val binding: ActivityLauncherBinding by lazy { ActivityLauncherBinding.inflate(layoutInflater) }

    private fun setUpCounters(playerCount: NumberPicker, roundCount: NumberPicker, savedState: GameConfiguration?) {
        playerCount.maxValue = MAX_PLAYERS
        playerCount.minValue = MIN_PLAYERS
        roundCount.maxValue = MAX_ROUNDS
        roundCount.minValue = MIN_ROUNDS

        if (savedState != null) {
            playerCount.value = savedState.playerCount
            roundCount.value = savedState.roundCount
        }
    }

    private fun startGame() {
        val drawIntent = Intent(this, DragGameActivity::class.java).apply {
            putExtra(GAME_CONFIGURATION_KEY, GameConfiguration(binding.playerCount.value, binding.roundCount.value))
            //addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        }
        startActivity(drawIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val savedState = savedInstanceState?.getParcelable<GameConfiguration>(GAME_CONFIGURATION_KEY)
        val gameState = savedInstanceState?.getParcelable<GameState>(GAME_STATE_KEY)
        if (gameState != null) {
            startGame()
        } else {
            setUpCounters(binding.playerCount, binding.roundCount, savedState)
            binding.startGameButton.setOnClickListener {startGame()}
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(GAME_CONFIGURATION_KEY, GameConfiguration(binding.playerCount.value, binding.roundCount.value))
    }
}