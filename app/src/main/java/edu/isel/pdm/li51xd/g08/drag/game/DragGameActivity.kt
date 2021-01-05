package edu.isel.pdm.li51xd.g08.drag.game

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.*
import edu.isel.pdm.li51xd.g08.drag.R.string
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityDrawBinding
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import edu.isel.pdm.li51xd.g08.drag.game.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState.State.*
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode.ONLINE
import edu.isel.pdm.li51xd.g08.drag.game.model.Word
import edu.isel.pdm.li51xd.g08.drag.utils.CountDownTimerAdapter
import edu.isel.pdm.li51xd.g08.drag.utils.EditTextNoEnter

class DragGameActivity : AppCompatActivity() {

    private val binding: ActivityDrawBinding by lazy { ActivityDrawBinding.inflate(layoutInflater) }
    private val viewModel: DragGameViewModel by viewModels()

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

    private fun drawDrawing(wordToDraw: Word) {
        binding.drawing.visibility = VISIBLE
        binding.drawing.isEnabled = true
        binding.drawing.clearCanvas() // Current drawing will be drawn in the view's OnSizeChanged

        binding.drawingWord.isEnabled = false
        binding.drawingWord.setText(wordToDraw.word)

        viewModel.scheduleWork(DRAWGUESS_TIME) {
            timer?.cancel()
            this.timeLeft = DRAWGUESS_TIME
            startTimer(DRAWGUESS_TIME)

            viewModel.defineDrawing()
        }
    }

    private fun drawGuessing(drawingToGuess: Drawing) {
        binding.drawing.visibility = VISIBLE
        binding.drawing.isEnabled = false
        binding.drawing.drawModel(drawingToGuess)

        binding.drawingWord.isEnabled = true
        binding.drawingWord.setText("")
        binding.drawingWord.setHint(string.guessHint)

        viewModel.scheduleWork(DRAWGUESS_TIME) {
            timer?.cancel()
            this.timeLeft = DRAWGUESS_TIME
            startTimer(DRAWGUESS_TIME)

            viewModel.defineGuess(binding.drawingWord.text.toString())
            binding.drawing.clearCanvas()
        }
    }

    private fun drawResults() {
        viewModel.clearSubscription()
        startActivity(Intent(this, DragResultsActivity::class.java).apply {
            putExtra(GAME_MODE_KEY, viewModel.gameMode.name)
            putExtra(GAME_CONFIGURATION_KEY, viewModel.config)
            putExtra(GAME_STATE_KEY, viewModel.game)

            putExtra(PLAYER_KEY, viewModel.player)
            putStringArrayListExtra(WORDS_KEY, viewModel.words)
            if (viewModel.gameMode == ONLINE) {
                putExtra(GAME_INFO_KEY, viewModel.gameInfo)
            }
        })
        finish()
    }

    private fun updateActivity(drawGuess: DrawGuess?) {
        when (viewModel.game.state) {
            DEFINING -> viewModel.startGame()
            DRAWING -> drawDrawing(drawGuess as Word)
            GUESSING -> drawGuessing(drawGuess as Drawing)
            RESULTS -> drawResults()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        timeLeft = savedInstanceState?.getLong(COUNTDOWN_TIME_LEFT_KEY, DRAWGUESS_TIME) ?: DRAWGUESS_TIME
        startTimer(timeLeft)

        binding.drawingWord.addTextChangedListener(EditTextNoEnter())
        binding.drawing.setOnNewVectorListener { x: Float, y: Float -> viewModel.addVectorToModel(x, y) }
        binding.drawing.setOnNewPointListener { x: Float, y: Float -> viewModel.addPointToModel(x, y) }
        binding.drawing.setOnSizeChangeListener {
            when (viewModel.game.state) {
                DRAWING -> binding.drawing.drawModel(viewModel.getCurrentDrawing())
                GUESSING -> binding.drawing.drawModel(viewModel.currentDrawGuess.value as Drawing)
                else -> {}
            }
        }

        viewModel.currentDrawGuess.observe(this) { drawGuess ->
            updateActivity(drawGuess)
        }

        if (viewModel.game.state == DEFINING) {
            viewModel.startGame()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(COUNTDOWN_TIME_LEFT_KEY, timeLeft)
        timer?.cancel()
    }
}