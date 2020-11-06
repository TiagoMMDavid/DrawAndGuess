package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLauncherBinding
import kotlinx.android.parcel.Parcelize


private const val MIN_PLAYERS = 5
private const val MAX_PLAYERS = 10

private const val MIN_ROUNDS = 1
private const val MAX_ROUNDS = 10

const val LAUNCHER_STATE_KEY = "DRAG.LauncherState"

@Parcelize
data class LauncherState(val playerCount: Int, val roundCount: Int) : Parcelable

class DragLauncher : AppCompatActivity() {
    private val binding: ActivityLauncherBinding by lazy { ActivityLauncherBinding.inflate(layoutInflater) }

    private fun setUpCounters(playerCount: NumberPicker, roundCount: NumberPicker, savedState: LauncherState?) {
        playerCount.maxValue = MAX_PLAYERS
        playerCount.minValue = MIN_PLAYERS
        roundCount.maxValue = MAX_ROUNDS
        roundCount.minValue = MIN_ROUNDS

        if (savedState != null) {
            playerCount.value = savedState.playerCount
            roundCount.value = savedState.roundCount
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val savedState = savedInstanceState?.getParcelable<LauncherState>(LAUNCHER_STATE_KEY)
        setUpCounters(binding.playerCount, binding.roundCount, savedState)

        binding.startGameButton.setOnClickListener {
            val myIntent = Intent(this, DragDraw::class.java).apply {
                putExtra(LAUNCHER_STATE_KEY, LauncherState(binding.playerCount.value, binding.roundCount.value))
            }
            startActivity(myIntent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(LAUNCHER_STATE_KEY, LauncherState(binding.playerCount.value, binding.roundCount.value))
    }
}