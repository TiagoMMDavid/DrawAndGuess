package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.os.Bundle
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLauncherBinding
import edu.isel.pdm.li51xd.g08.drag.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.DEFINING
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.DRAWING
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.GUESSING
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.LAUNCHER
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.RESULTS
import edu.isel.pdm.li51xd.g08.drag.model.MAX_PLAYERS
import edu.isel.pdm.li51xd.g08.drag.model.MAX_ROUNDS
import edu.isel.pdm.li51xd.g08.drag.model.MIN_PLAYERS
import edu.isel.pdm.li51xd.g08.drag.model.MIN_ROUNDS
import edu.isel.pdm.li51xd.g08.drag.model.Repository

class DragLauncher : AppCompatActivity() {
    private val binding: ActivityLauncherBinding by lazy { ActivityLauncherBinding.inflate(layoutInflater) }
    private val repo: Repository by lazy { (application as DragApplication).repo }

    private fun setUpLayout(playerCount: NumberPicker, roundCount: NumberPicker, config: GameConfiguration) {
        playerCount.maxValue = MAX_PLAYERS
        playerCount.minValue = MIN_PLAYERS
        roundCount.maxValue = MAX_ROUNDS
        roundCount.minValue = MIN_ROUNDS

        playerCount.value = config.playerCount
        roundCount.value = config.roundCount

        binding.startGameButton.setOnClickListener { startGame() }
    }

    private fun startGame() {
        repo.game.state = DEFINING
        startActivity(Intent(this, DragGameActivity::class.java))
    }

    private fun startResults() {
        startActivity(Intent(this, DragResultsActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        when(repo.game.state) {
            LAUNCHER -> setUpLayout(binding.playerCount, binding.roundCount, repo.config)
            DEFINING, DRAWING, GUESSING -> startGame()
            RESULTS -> startResults()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        repo.config = GameConfiguration(binding.playerCount.value, binding.roundCount.value)
    }
}