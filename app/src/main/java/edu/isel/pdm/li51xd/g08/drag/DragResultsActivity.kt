package edu.isel.pdm.li51xd.g08.drag

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityResultsBinding
import edu.isel.pdm.li51xd.g08.drag.model.DrawGuess.ResultType.DRAWING
import edu.isel.pdm.li51xd.g08.drag.model.DrawGuess.ResultType.WORD
import edu.isel.pdm.li51xd.g08.drag.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.model.GameState
import edu.isel.pdm.li51xd.g08.drag.model.Word
import edu.isel.pdm.li51xd.g08.drag.utils.DrawingListener
import edu.isel.pdm.li51xd.g08.drag.view.DrawingView


class DragResultsActivity : AppCompatActivity() {
    private val binding: ActivityResultsBinding by lazy { ActivityResultsBinding.inflate(layoutInflater) }
    lateinit var game: GameState
    lateinit var config: GameConfiguration

    private fun getGameContext(extras: Bundle?, savedInstanceState: Bundle?) {
        when {
            extras != null -> {
                game = extras.getParcelable(GAME_STATE_KEY)!!
                config = extras.getParcelable(GAME_CONFIGURATION_KEY)!!
            }
            savedInstanceState != null -> {
                game = savedInstanceState.getParcelable(GAME_STATE_KEY)!!
                config = savedInstanceState.getParcelable(GAME_CONFIGURATION_KEY)!!
            }
            else -> {
                throw IllegalStateException()
            }
        }
    }

    private fun startGame() {
        val nextRound = game.currRound + 1
        val myIntent = Intent(this, DragGameActivity::class.java).apply {
            putExtra(GAME_CONFIGURATION_KEY, config)
            putExtra(GAME_STATE_KEY, GameState(currRound = nextRound))
        }
        startActivity(myIntent)
    }

    private fun drawResults() {
        val resultsView = binding.resultsView
        game.drawGuesses.forEach {
            when(it.getResultType()) {
                DRAWING -> {
                    val drawingView = DrawingView(applicationContext, null)
                    resultsView.addView(drawingView)
                    drawingView.isEnabled = false
                    drawingView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCanvas))
                    drawingView.setOnDrawChangeListener(object: DrawingListener {
                        override fun onNewPoint(x: Float, y: Float, isInitial: Boolean) {
                        }

                        override fun onSizeChange() {
                            drawingView.drawModel(it as Drawing)
                        }
                    })
                }
                WORD -> {
                    val wordView = TextView(applicationContext, null)
                    resultsView.addView(wordView)
                    wordView.text = (it as Word).word
                }
            }
        }
    }

    private fun setUpLayout() {
        val currRound = game.currRound
        val totalRounds = config.roundCount

        binding.roundEndText.text = getString(R.string.roundEnding, currRound, totalRounds)

        if (currRound == totalRounds) {
            binding.finishButton.setText(R.string.finishGame)
            binding.finishButton.setOnClickListener {
                finish()
            }
        } else {
            binding.finishButton.setText(R.string.finishRound)
            binding.finishButton.setOnClickListener {
                startGame()
                // TODO: NÃO FAZER FINISH
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getGameContext(intent.extras, savedInstanceState)

        setUpLayout()
        drawResults()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(GAME_CONFIGURATION_KEY, config)
        outState.putParcelable(GAME_STATE_KEY, game)
    }
}