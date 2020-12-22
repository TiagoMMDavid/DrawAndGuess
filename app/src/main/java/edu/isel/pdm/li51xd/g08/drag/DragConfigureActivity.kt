package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLauncherBinding
import edu.isel.pdm.li51xd.g08.drag.model.*
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.DEFINING

class DragConfigureActivity : AppCompatActivity() {
    private val binding: ActivityLauncherBinding by lazy { ActivityLauncherBinding.inflate(layoutInflater) }
    private val repo: Repository by lazy { (application as DragApplication).repo }

    private fun setUpLayout() {
        binding.playerCount.maxValue = MAX_PLAYERS
        binding.playerCount.minValue = MIN_PLAYERS
        binding.roundCount.maxValue = MAX_ROUNDS
        binding.roundCount.minValue = MIN_ROUNDS

        binding.playerCount.value = repo.config.playerCount
        binding.roundCount.value = repo.config.roundCount

        binding.startGameButton.setOnClickListener { startGame() }
    }

    private fun startGame() {
        repo.game.state = DEFINING
        repo.config = GameConfiguration(binding.playerCount.value, binding.roundCount.value, repo.config.mode)
        startActivity(Intent(this, DragGameActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpLayout()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        repo.config = GameConfiguration(binding.playerCount.value, binding.roundCount.value, repo.config.mode)
    }
}