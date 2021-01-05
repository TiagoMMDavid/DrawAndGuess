package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLauncherBinding
import edu.isel.pdm.li51xd.g08.drag.game.DragConfigureActivity
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode
import edu.isel.pdm.li51xd.g08.drag.remote.DragListGamesActivity

class DragLauncherActivity : AppCompatActivity() {
    private val binding: ActivityLauncherBinding by lazy { ActivityLauncherBinding.inflate(layoutInflater) }

    private fun changeButtonsState(isEnabled: Boolean) {
        binding.offlineButton.isEnabled = isEnabled
        binding.onlineButton.isEnabled = isEnabled
    }

    private fun setUpLayout() {
        binding.offlineButton.setOnClickListener {
            startActivity(Intent(this, DragConfigureActivity::class.java).apply {
                changeButtonsState(false)
                putExtra(GAME_MODE_KEY, Mode.OFFLINE.name)
            })
        }

        binding.onlineButton.setOnClickListener {
            changeButtonsState(false)
            startActivity(Intent(this, DragListGamesActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpLayout()
    }

    override fun onResume() {
        super.onResume()
        changeButtonsState(true)
    }
}