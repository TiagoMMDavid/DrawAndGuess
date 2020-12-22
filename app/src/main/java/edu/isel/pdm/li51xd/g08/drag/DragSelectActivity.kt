package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivitySelectBinding
import edu.isel.pdm.li51xd.g08.drag.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.model.GameConfiguration.Mode.*
import edu.isel.pdm.li51xd.g08.drag.model.GameState
import edu.isel.pdm.li51xd.g08.drag.model.Repository

private const val SELECT_ONLINE_BUTTON = "DRAG.SelectOnlineButton"

class DragSelectActivity : AppCompatActivity() {
    private val binding: ActivitySelectBinding by lazy { ActivitySelectBinding.inflate(layoutInflater) }
    private val repo: Repository by lazy { (application as DragApplication).repo }

    private fun setUpLayout(savedInstanceState: Bundle?) {
        var isOnline = false
        if (savedInstanceState != null) {
            isOnline = savedInstanceState.getBoolean(SELECT_ONLINE_BUTTON)
        }
        if (isOnline) {
            binding.createGameButton.visibility = VISIBLE
            binding.joinGameButton.visibility = VISIBLE
            binding.onlineButton.isEnabled = false
        } else {
            binding.createGameButton.visibility = INVISIBLE
            binding.joinGameButton.visibility = INVISIBLE
        }

        binding.offlineButton.setOnClickListener {
            repo.config = GameConfiguration(mode = OFFLINE)
            repo.game.state = GameState.State.CONFIGURE
            startActivity(Intent(this, DragConfigureActivity::class.java))
        }
        binding.onlineButton.setOnClickListener {
            binding.onlineButton.isEnabled = false
            binding.createGameButton.visibility = VISIBLE
            binding.joinGameButton.visibility = VISIBLE
        }
        binding.createGameButton.setOnClickListener {
            repo.config = GameConfiguration(mode = ONLINE)
            repo.game.state = GameState.State.CONFIGURE
            startActivity(Intent(this, DragConfigureActivity::class.java))
        }
        binding.joinGameButton.setOnClickListener {
            repo.config = GameConfiguration(mode = ONLINE)
            repo.game.state = GameState.State.LIST
            TODO("start list activity")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        when(repo.game.state) {
            GameState.State.LAUNCHER -> setUpLayout(savedInstanceState)
            GameState.State.LIST -> TODO()
            GameState.State.CONFIGURE -> startActivity(Intent(this, DragConfigureActivity::class.java))
            GameState.State.LOBBY -> TODO()
            GameState.State.DEFINING -> startActivity(Intent(this, DragGameActivity::class.java))
            GameState.State.DRAWING -> startActivity(Intent(this, DragGameActivity::class.java))
            GameState.State.GUESSING -> startActivity(Intent(this, DragGameActivity::class.java))
            GameState.State.RESULTS -> startActivity(Intent(this, DragResultsActivity::class.java))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SELECT_ONLINE_BUTTON, !binding.onlineButton.isEnabled)
    }

}