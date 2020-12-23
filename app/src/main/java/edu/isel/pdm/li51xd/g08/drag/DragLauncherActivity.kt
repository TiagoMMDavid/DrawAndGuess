package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLauncherBinding
import edu.isel.pdm.li51xd.g08.drag.model.GAME_MODE_KEY
import edu.isel.pdm.li51xd.g08.drag.model.GameConfiguration

class DragLauncherActivity : AppCompatActivity() {
    private val binding: ActivityLauncherBinding by lazy { ActivityLauncherBinding.inflate(layoutInflater) }

    private fun setUpLayout() {
        binding.offlineButton.setOnClickListener {
            startActivity(Intent(this, DragConfigureActivity::class.java).apply {
                putExtra(GAME_MODE_KEY, GameConfiguration.Mode.getMode(false).name)
            })
        }

        binding.onlineButton.setOnClickListener {
            TODO("Start List lobbies activity")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpLayout()
    }
}