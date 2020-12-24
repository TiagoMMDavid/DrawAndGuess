package edu.isel.pdm.li51xd.g08.drag.game

import android.content.Intent
import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.R.string
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityDrawBinding
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_CONFIGURATION_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_STATE_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState.State.DRAWING
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState.State.GUESSING
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState.State.RESULTS
import edu.isel.pdm.li51xd.g08.drag.game.model.Word
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.utils.EditTextNoEnter

class DragGameActivity : AppCompatActivity() {
    private val binding: ActivityDrawBinding by lazy { ActivityDrawBinding.inflate(layoutInflater) }
    private val viewModel: DrawViewModel by viewModels()

    private fun drawDrawing() {
        binding.drawing.visibility = VISIBLE
        binding.drawing.isEnabled = true

        binding.drawingWord.isEnabled = false
        binding.drawingWord.setText((viewModel.getLastDrawGuess() as Word).word)

        binding.submitButton.setOnClickListener {
            viewModel.defineDrawing()
        }
    }

    private fun drawGuessing(toast: Toast) {
        binding.drawing.visibility = VISIBLE
        binding.drawing.isEnabled = false

        binding.drawingWord.isEnabled = true
        binding.drawingWord.setText("")
        binding.drawingWord.setHint(string.guessHint)

        binding.submitButton.setOnClickListener {
            val text = binding.drawingWord.text.toString()
            if (text.isNotEmpty()) {
                viewModel.defineGuess(text)
                binding.drawing.clearCanvas()
            } else {
                toast.show()
            }
        }
    }

    private fun drawResults() {
        startActivity(Intent(this, DragResultsActivity::class.java).apply {
            putExtra(GAME_CONFIGURATION_KEY, viewModel.config)
            putExtra(GAME_STATE_KEY, viewModel.game)
            putStringArrayListExtra(WORDS_KEY, viewModel.words)
        })
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val toast = Toast.makeText(applicationContext, getString(string.guessEmpty), Toast.LENGTH_SHORT)

        binding.drawingWord.addTextChangedListener(EditTextNoEnter())
        binding.drawing.setOnNewVectorListener { x: Float, y: Float -> viewModel.addVectorToModel(x, y) }
        binding.drawing.setOnNewPointListener { x: Float, y: Float -> viewModel.addPointToModel(x, y) }
        binding.drawing.setOnSizeChangeListener { binding.drawing.drawModel(viewModel.getCurrentDrawing()) }

        viewModel.setOnStateChangeListener { state ->
            when (state) {
                DRAWING -> drawDrawing()
                GUESSING -> drawGuessing(toast)
                RESULTS -> drawResults()
            }
        }
        viewModel.startGame()
    }
}