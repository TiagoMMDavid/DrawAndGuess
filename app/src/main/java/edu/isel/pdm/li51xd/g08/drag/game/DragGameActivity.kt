package edu.isel.pdm.li51xd.g08.drag.game

import android.content.Intent
import android.os.Bundle
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.R.string
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityDrawBinding
import edu.isel.pdm.li51xd.g08.drag.game.model.*
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState.State.*
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.utils.EditTextNoEnter

class DragGameActivity : AppCompatActivity() {
    private val binding: ActivityDrawBinding by lazy { ActivityDrawBinding.inflate(layoutInflater) }
    private val viewModel: DrawViewModel by viewModels()

    private fun drawDrawing(wordToDraw: Word) {
        binding.drawing.visibility = VISIBLE
        binding.drawing.isEnabled = true
        binding.drawing.clearCanvas() // Current drawing will be drawn in the view's OnSizeChanged

        binding.drawingWord.isEnabled = false
        binding.drawingWord.setText(wordToDraw.word)

        viewModel.scheduleWork(DRAWGUESS_TIME) { viewModel.defineDrawing() }
        /*
        binding.submitButton.setOnClickListener {
            viewModel.defineDrawing()
        } */
    }

    private fun drawGuessing(drawingToGuess: Drawing) {
        binding.drawing.visibility = VISIBLE
        binding.drawing.isEnabled = false
        binding.drawing.drawModel(drawingToGuess)

        binding.drawingWord.isEnabled = true
        binding.drawingWord.setText("")
        binding.drawingWord.setHint(string.guessHint)

        viewModel.scheduleWork(DRAWGUESS_TIME) {
            viewModel.defineGuess(binding.drawingWord.text.toString())
            binding.drawing.clearCanvas()
        }
        /*
        binding.submitButton.setOnClickListener {
            val text = binding.drawingWord.text.toString()
            if (text.isNotEmpty()) {
                viewModel.defineGuess(text)
                binding.drawing.clearCanvas()
            } else {
                Toast.makeText(applicationContext, string.guessEmpty, Toast.LENGTH_SHORT).show()
            }
        } */
    }

    private fun drawResults() {
        startActivity(Intent(this, DragResultsActivity::class.java).apply {
            putExtra(GAME_CONFIGURATION_KEY, viewModel.config)
            putExtra(GAME_STATE_KEY, viewModel.game)
            putStringArrayListExtra(WORDS_KEY, viewModel.words)
        })
        finish()
    }

    private fun updateActivity(drawGuess: DrawGuess?) {
        when (viewModel.game.state) {
            DEFINING -> viewModel.startGame()
            RESULTS -> drawResults()
            DRAWING -> drawDrawing(drawGuess as Word)
            GUESSING -> drawGuessing(drawGuess as Drawing)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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
}