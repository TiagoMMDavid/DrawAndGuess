package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityDrawBinding
import edu.isel.pdm.li51xd.g08.drag.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.DEFINING
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.DRAWING
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.GUESSING
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.RESULTS
import edu.isel.pdm.li51xd.g08.drag.model.Word
import edu.isel.pdm.li51xd.g08.drag.utils.DrawingListener
import edu.isel.pdm.li51xd.g08.drag.utils.EditTextNoEnter

class DragGameActivity : AppCompatActivity() {
    private val binding: ActivityDrawBinding by lazy { ActivityDrawBinding.inflate(layoutInflater) }
    private val viewModel: DrawViewModel by viewModels()

    private fun drawDefining(toast: Toast) {
        binding.drawing.visibility = INVISIBLE
        binding.drawing.isEnabled = false

        binding.drawingWord.isEnabled = true
        binding.drawingWord.hint = getString(R.string.definingHint)
        binding.submitButton.setOnClickListener {
            val text = binding.drawingWord.text.toString()
            if (text.isNotEmpty()) {
                viewModel.defineWord(text)
            } else {
                toast.show()
            }
        }
    }

    private fun drawDrawing() {
        binding.drawing.visibility = VISIBLE
        binding.drawing.isEnabled = true

        binding.drawingWord.isEnabled = false
        binding.drawingWord.setText((viewModel.game.drawGuesses.last() as Word).word)

        binding.submitButton.setOnClickListener {
            viewModel.defineDrawing()
        }
    }

    private fun drawGuessing(toast: Toast) {
        binding.drawing.visibility = VISIBLE
        binding.drawing.isEnabled = false

        binding.drawingWord.isEnabled = true
        binding.drawingWord.setText("")
        binding.drawingWord.setHint(R.string.guessHint)

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
        viewModel.game.currRound
        val myIntent = Intent(this, DragResultsActivity::class.java).apply {
            putExtra(GAME_CONFIGURATION_KEY, viewModel.config)
            putExtra(GAME_STATE_KEY, viewModel.game)
        }
        startActivity(myIntent)
        // TODO: NÃO FAZER FINISH
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val toast = Toast.makeText(applicationContext, getString(R.string.guessEmpty), Toast.LENGTH_SHORT)

        binding.drawingWord.addTextChangedListener(EditTextNoEnter())
        binding.drawing.setOnDrawChangeListener(object: DrawingListener {
            override fun onNewPoint(x: Float, y: Float, isInitial: Boolean) {
                viewModel.addPoint(x, y, isInitial)
            }

            override fun onSizeChange() {
                binding.drawing.drawModel(viewModel.game.currentDrawing)
            }
        })

        viewModel.setOnStateChangeListener {state ->
            when(state) {
                DEFINING -> drawDefining(toast)
                DRAWING -> drawDrawing()
                GUESSING -> drawGuessing(toast)
                RESULTS -> drawResults()
            }
        }
        viewModel.startGame()
    }
}