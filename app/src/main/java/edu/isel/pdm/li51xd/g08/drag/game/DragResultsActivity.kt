package edu.isel.pdm.li51xd.g08.drag.game

import android.R.layout
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.R.string
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityResultsBinding
import edu.isel.pdm.li51xd.g08.drag.game.model.*
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType.DRAWING
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType.WORD
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.ONLINE
import edu.isel.pdm.li51xd.g08.drag.game.remote.GameInfo
import edu.isel.pdm.li51xd.g08.drag.game.remote.Player
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.utils.CountDownTimerAdapter
import edu.isel.pdm.li51xd.g08.drag.utils.OnItemSelectedListenerAdapter

private const val RESULT_INDEX_KEY = "DRAG.ResultIndex"

private const val INDEX_BAR_ANIMATION_SMOOTHNESS = 100 // Can be seen as frames per index change
private const val INDEX_BAR_ANIMATION_DURATION = 250L

class DragResultsActivity : AppCompatActivity() {
    private val binding: ActivityResultsBinding by lazy { ActivityResultsBinding.inflate(layoutInflater) }
    private val viewModel: DragResultsViewModel by viewModels()

    private var isLastRound : Boolean = true

    private var currResultIndex = 0
    private var timer: CountDownTimer? = null
    private var timeLeft: Long = DRAWGUESS_TIME

    private fun startTimer(time: Long) {
        // Set max progress to 1 second less in order to present the bar as full when the timer starts
        binding.drawGuessTimerProgress.max = DRAWGUESS_TIME.toInt() - 1000
        binding.drawGuessTimerProgress.progress = DRAWGUESS_TIME.toInt() - 1000

        timer = CountDownTimerAdapter(time, COUNTDOWN_INTERVAL) { millisUntilFinished ->
            timeLeft = millisUntilFinished
            binding.drawGuessTimerText.text = "${millisUntilFinished / COUNTDOWN_INTERVAL}"
            binding.drawGuessTimerProgress.progress = (millisUntilFinished - COUNTDOWN_INTERVAL).toInt()
        }
        timer?.start()
    }

    private fun startGame() {
        val nextRound = viewModel.game.currRound + 1
        startActivity(Intent(this, DragGameActivity::class.java).apply {
            putExtra(GAME_STATE_KEY, GameState(currRound = nextRound))
            putExtra(GAME_CONFIGURATION_KEY, viewModel.config)
            putStringArrayListExtra(WORDS_KEY, viewModel.words)
            putExtra(GAME_MODE_KEY, viewModel.gameMode.name)
            if (viewModel.gameMode == ONLINE) {
                putExtra(GAME_INFO_KEY, GameInfo(viewModel.getNextRoundString(), viewModel.gameInfo!!.players))
                putExtra(PLAYER_KEY, viewModel.player)
            }
        })
        finish()
    }

    private fun drawResult(drawGuess: DrawGuess) {
        when(drawGuess.getType()) {
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

    private fun updateLayout(drawGuessList: List<DrawGuess>) {
        binding.swipeZone.isSwipeRightEnabled = currResultIndex > 0
        binding.swipeZone.isSwipeLeftEnabled = currResultIndex < drawGuessList.size - 1

        ObjectAnimator.ofInt(binding.indexBar, "progress", currResultIndex * INDEX_BAR_ANIMATION_SMOOTHNESS).apply {
            duration = INDEX_BAR_ANIMATION_DURATION
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun updateDrawGuesses(drawGuessList: List<DrawGuess>) {
        binding.indexBar.max = (drawGuessList.size - 1) * INDEX_BAR_ANIMATION_SMOOTHNESS

        binding.drawing.setOnSizeChangeListener {
            drawResult(drawGuessList[currResultIndex])
        }

        binding.swipeZone.setOnSwipeRight {
            drawResult(drawGuessList[--currResultIndex])
            updateLayout(drawGuessList)
        }
        binding.swipeZone.setOnSwipeLeft {
            drawResult(drawGuessList[++currResultIndex])
            updateLayout(drawGuessList)
        }

        updateLayout(drawGuessList)
        drawResult(drawGuessList[currResultIndex])
    }

    private fun setUpLayout() {
        binding.finishButton.setText(string.finishGame)
        binding.finishButton.setOnClickListener {
            viewModel.clearSubscription()
            viewModel.exitGame()
            finish()
        }

        binding.playerSelector.isEnabled = false
        binding.playerSelector.adapter = ArrayAdapter(this, layout.simple_spinner_dropdown_item, viewModel.getPlayers())
        binding.playerSelector.onItemSelectedListener = OnItemSelectedListenerAdapter<Player> {
            if (binding.playerSelector.isEnabled) {
                currResultIndex = 0
                viewModel.updateCurrentDrawGuesses(it.id)
            }
        }

        binding.roundEndText.text = getString(string.roundEnding, viewModel.game.currRound, viewModel.config.roundCount)
        binding.drawing.isEnabled = false

        drawResult(Word(getString(string.resultsWaiting)))
    }

    private fun finishGathering() {
        var id: String? = null
        if (viewModel.gameMode == ONLINE) {
            // Only enable the selector if we're online.
            binding.playerSelector.isEnabled = true

            // In offline mode, there's no need to update currentDrawGuesses
            id = (binding.playerSelector.getItemAtPosition(0) as Player).id
            viewModel.updateCurrentDrawGuesses(id)

            if (!isLastRound) {
                viewModel.createNextRound(id)
            }
        }

        if (!isLastRound) {
            binding.drawGuessTimer.visibility = View.VISIBLE
            startTimer(timeLeft)
            viewModel.scheduleWork(DRAWGUESS_TIME) {
                viewModel.clearSubscription()
                viewModel.deleteRound(id)
                startGame()
            }
        } else {
            binding.finishButton.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        timeLeft = savedInstanceState?.getLong(COUNTDOWN_TIME_LEFT_KEY, DRAWGUESS_TIME) ?: DRAWGUESS_TIME
        isLastRound = viewModel.game.currRound == viewModel.config.roundCount

        if (savedInstanceState != null)
            currResultIndex = savedInstanceState.getInt(RESULT_INDEX_KEY)

        setUpLayout()
        viewModel.currentDrawGuesses.observe(this) {
            if (it == null) {
                Toast.makeText(this, string.errorGetResults, Toast.LENGTH_LONG).show()
            } else {
                updateDrawGuesses(it)
            }
        }

        viewModel.gatherResults {
            finishGathering()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(RESULT_INDEX_KEY, currResultIndex)
        outState.putLong(COUNTDOWN_TIME_LEFT_KEY, timeLeft)
        timer?.cancel()
    }
}
