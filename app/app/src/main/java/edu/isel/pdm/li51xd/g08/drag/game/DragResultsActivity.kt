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
import edu.isel.pdm.li51xd.g08.drag.COUNTDOWN_INTERVAL
import edu.isel.pdm.li51xd.g08.drag.COUNTDOWN_TIME_LEFT_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_CONFIGURATION_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_INFO_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_MODE_KEY
import edu.isel.pdm.li51xd.g08.drag.GAME_STATE_KEY
import edu.isel.pdm.li51xd.g08.drag.PLAYER_KEY
import edu.isel.pdm.li51xd.g08.drag.R.string
import edu.isel.pdm.li51xd.g08.drag.RESULTS_TIME
import edu.isel.pdm.li51xd.g08.drag.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityResultsBinding
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType.DRAWING
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType.WORD
import edu.isel.pdm.li51xd.g08.drag.game.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState.State.DEFINING
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.ONLINE
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import edu.isel.pdm.li51xd.g08.drag.game.model.Word
import edu.isel.pdm.li51xd.g08.drag.remote.model.GameInfo
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
    private var timeLeft: Long = RESULTS_TIME

    private fun startTimer(time: Long) {
        // Set max progress to 1 second less in order to present the bar as full when the timer starts
        binding.drawGuessTimerProgress.max = RESULTS_TIME.toInt() - 1000
        binding.drawGuessTimerProgress.progress = RESULTS_TIME.toInt() - 1000

        timer = CountDownTimerAdapter(time, COUNTDOWN_INTERVAL) { millisUntilFinished ->
            timeLeft = millisUntilFinished
            binding.drawGuessTimerText.text = "${millisUntilFinished / COUNTDOWN_INTERVAL}"
            binding.drawGuessTimerProgress.progress = (millisUntilFinished - COUNTDOWN_INTERVAL).toInt()
        }
        timer?.start()
    }

    private fun startGame() {
        if (!viewModel.playerLeft) {
            startActivity(Intent(this, DragGameActivity::class.java).apply {
                if (viewModel.gameMode == ONLINE) {
                    putExtra(GAME_INFO_KEY, GameInfo(viewModel.getNextRoundId(), viewModel.gameInfo!!.players))
                }
                putExtra(GAME_MODE_KEY, viewModel.gameMode.name)
                putExtra(GAME_CONFIGURATION_KEY, viewModel.config)
                putExtra(GAME_STATE_KEY, viewModel.game.apply { ++currRound; state = DEFINING })

                putExtra(PLAYER_KEY, viewModel.player)
                putStringArrayListExtra(WORDS_KEY, viewModel.words)
            })
        } else {
            viewModel.deleteNextRound()
            viewModel.exitGame()
            Toast.makeText(this, getString(string.errorInGame), Toast.LENGTH_LONG).show()
        }
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

    private fun setupLayout() {
        binding.finishButton.setText(string.finishGame)
        binding.finishButton.setOnClickListener {
            viewModel.clearSubscription()
            viewModel.exitGame()
            finish()
        }

        binding.playerSelector.isEnabled = viewModel.currentDrawGuesses.value != null && viewModel.gameMode == ONLINE
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

    private fun setupTimerAndButton() {
        if (!isLastRound) {
            binding.drawGuessTimer.visibility = View.VISIBLE
            startTimer(timeLeft)
            viewModel.scheduleWork(timeLeft) {
                viewModel.clearSubscription()
                viewModel.deleteRound()
                startGame()
            }
        } else {
            binding.finishButton.visibility = View.VISIBLE
        }
    }

    private fun finishGathering() {
        if (viewModel.gameMode == ONLINE) {
            // Only enable the selector if we're online.
            binding.playerSelector.isEnabled = true

            // In offline mode, there's no need to update currentDrawGuesses
            val id = (binding.playerSelector.getItemAtPosition(0) as Player).id
            viewModel.updateCurrentDrawGuesses(id)

            if (!isLastRound) {
                viewModel.createNextRound(id)
            }
        }

        setupTimerAndButton()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        timeLeft = savedInstanceState?.getLong(COUNTDOWN_TIME_LEFT_KEY, RESULTS_TIME) ?: RESULTS_TIME
        isLastRound = viewModel.game.currRound == viewModel.config.roundCount

        if (savedInstanceState != null)
            currResultIndex = savedInstanceState.getInt(RESULT_INDEX_KEY)

        setupLayout()
        viewModel.currentDrawGuesses.observe(this) {
            if (it == null) {
                Toast.makeText(this, string.errorGetResults, Toast.LENGTH_LONG).show()
            } else {
                updateDrawGuesses(it)
            }
        }

        if (viewModel.currentDrawGuesses.value == null) {
            viewModel.gatherResults {
                finishGathering()
            }
        } else {
            setupTimerAndButton()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(RESULT_INDEX_KEY, currResultIndex)
        outState.putLong(COUNTDOWN_TIME_LEFT_KEY, timeLeft)
        timer?.cancel()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        viewModel.clearSubscription()
        viewModel.exitGame()
        viewModel.cancelWork()
    }
}
