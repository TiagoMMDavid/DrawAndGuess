package edu.isel.pdm.li51xd.g08.drag

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLauncherBinding

class DragLauncher : AppCompatActivity() {
    private val binding: ActivityLauncherBinding by lazy { ActivityLauncherBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}