package edu.isel.pdm.li51xd.g08.drag

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityResultsBinding
import edu.isel.pdm.li51xd.g08.drag.model.*
import edu.isel.pdm.li51xd.g08.drag.model.DrawGuess.ResultType.DRAWING
import edu.isel.pdm.li51xd.g08.drag.model.DrawGuess.ResultType.WORD
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.DEFINING

private const val RESULT_INDEX_KEY = "DRAG.ResultIndex"

private const val INDEX_BAR_ANIMATION_SMOOTHNESS = 100 // Can be seen as frames per index change
private const val INDEX_BAR_ANIMATION_DURATION = 250L

class DragResultsActivity : AppCompatActivity() {
    private val binding: ActivityResultsBinding by lazy { ActivityResultsBinding.inflate(layoutInflater) }

    private val game: GameState by lazy {
        intent.getParcelableExtra<GameState>(GAME_STATE_KEY) ?: throw IllegalArgumentException()
    }
    private val config: GameConfiguration by lazy {
        intent.getParcelableExtra<GameConfiguration>(GAME_CONFIGURATION_KEY) ?: throw IllegalArgumentException()
    }

    private var currResultIndex = 0

    private fun startGame() {
        val nextRound = game.currRound + 1
        startActivity(Intent(this, DragGameActivity::class.java).apply {
            putExtra(GAME_STATE_KEY, GameState(currRound = nextRound, state = DEFINING))
            putExtra(GAME_CONFIGURATION_KEY, config)
        })
        finish()
    }

    private fun drawResult(drawGuess: DrawGuess) {
        when(drawGuess.getResultType()) {
            DRAWING -> {
                binding.drawing.drawModel(drawGuess as Drawing)
                binding.drawing.visibility = View.VISIBLE
                binding.guessText.visibility = View.INVISIBLE
            }
            WORD -> {
                binding.guessText.text = (drawGuess as Word).word
                binding.guessText.visibility = View.VISIBLE
                binding.drawing.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateLayout() {
        binding.swipeZone.isSwipeRightEnabled = currResultIndex > 0
        binding.swipeZone.isSwipeLeftEnabled = currResultIndex < game.drawGuesses.size - 1

        ObjectAnimator.ofInt(binding.indexBar, "progress", currResultIndex * INDEX_BAR_ANIMATION_SMOOTHNESS).apply {
            duration = INDEX_BAR_ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun setUpLayout() {
        val currRound = game.currRound
        val totalRounds = config.roundCount

        if (currRound == totalRounds) {
            binding.finishButton.setText(R.string.finishGame)
            binding.finishButton.setOnClickListener {
                finish()
            }
        } else {
            binding.finishButton.setText(R.string.finishRound)
            binding.finishButton.setOnClickListener {
                startGame()
            }
        }

        binding.roundEndText.text = getString(R.string.roundEnding, currRound, totalRounds)
        binding.indexBar.max = (game.drawGuesses.size - 1) * INDEX_BAR_ANIMATION_SMOOTHNESS

        binding.drawing.isEnabled = false
        binding.drawing.setOnSizeChangeListener {
            drawResult(game.drawGuesses[currResultIndex])
        }
        updateLayout()

        binding.swipeZone.setOnSwipeRight {
            drawResult(game.drawGuesses[--currResultIndex])
            updateLayout()
        }
        binding.swipeZone.setOnSwipeLeft {
            drawResult(game.drawGuesses[++currResultIndex])
            updateLayout()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState != null)
            currResultIndex = savedInstanceState.getInt(RESULT_INDEX_KEY)

        setUpLayout()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(RESULT_INDEX_KEY, currResultIndex)
    }
}