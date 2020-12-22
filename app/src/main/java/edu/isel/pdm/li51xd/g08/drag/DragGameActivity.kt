package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityDrawBinding
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.DEFINING
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.DRAWING
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.GUESSING
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.RESULTS
import edu.isel.pdm.li51xd.g08.drag.model.Repository
import edu.isel.pdm.li51xd.g08.drag.model.Word
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
        startActivity(Intent(this, DragResultsActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val toast = Toast.makeText(applicationContext, getString(R.string.guessEmpty), Toast.LENGTH_SHORT)

        binding.drawingWord.addTextChangedListener(EditTextNoEnter())
        binding.drawing.setOnNewVectorListener { x: Float, y: Float -> viewModel.addVectorToModel(x, y) }
        binding.drawing.setOnNewPointListener { x: Float, y: Float -> viewModel.addPointToModel(x, y) }
        binding.drawing.setOnSizeChangeListener { binding.drawing.drawModel(viewModel.getCurrentDrawing()) }

        viewModel.setOnStateChangeListener { state ->
            when (state) {
                DEFINING -> drawDefining(toast)
                DRAWING -> drawDrawing()
                GUESSING -> drawGuessing(toast)
                RESULTS -> drawResults()
                else -> throw IllegalStateException()
            }
        }
        viewModel.startGame()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        (application as DragApplication).repo.reset()
    }
}