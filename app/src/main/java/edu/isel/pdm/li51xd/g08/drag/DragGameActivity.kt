package edu.isel.pdm.li51xd.g08.drag

import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityDrawBinding
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

    private fun drawDefining() {
        binding.drawing.visibility = INVISIBLE
        binding.drawing.isEnabled = false

        binding.drawingWord.isEnabled = true
        binding.drawingWord.hint = getString(R.string.definingHint)
        binding.submitButton.setOnClickListener {
            val text = binding.drawingWord.text.toString()
            if (text.isNotEmpty()) {
                viewModel.defineWord(text)
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

    private fun drawGuessing() {
        binding.drawing.visibility = VISIBLE
        binding.drawing.isEnabled = false

        binding.drawingWord.isEnabled = true
        binding.drawingWord.setText("")
        binding.drawingWord.setHint(R.string.guessHint)

        binding.submitButton.setOnClickListener {
            val text = binding.drawingWord.text.toString()
            if (text.isNotEmpty()) {
                viewModel.defineGuess(text)
                binding.drawing.clear()
            }
        }
    }

    private fun drawResults() {
        TODO()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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
                DEFINING -> drawDefining()
                DRAWING -> drawDrawing()
                GUESSING -> drawGuessing()
                RESULTS -> drawResults()
            }
        }
        viewModel.startGame()
    }
}