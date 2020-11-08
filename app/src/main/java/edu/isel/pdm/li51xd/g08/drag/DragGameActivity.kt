package edu.isel.pdm.li51xd.g08.drag

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityDrawBinding
import edu.isel.pdm.li51xd.g08.drag.utils.DrawingListener

class DragGameActivity : AppCompatActivity() {
    private val binding: ActivityDrawBinding by lazy { ActivityDrawBinding.inflate(layoutInflater) }
    private val viewModel: DrawViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.drawing.setOnDrawChangeListener(object: DrawingListener {
            override fun onNewPoint(x: Float, y: Float, isInitial: Boolean) {
                viewModel.addPoint(x, y, isInitial)
            }

            override fun onSizeChange() {
                binding.drawing.drawModel(viewModel.game.currentDrawing)
            }
        })
    }
}