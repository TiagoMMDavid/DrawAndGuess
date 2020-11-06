package edu.isel.pdm.li51xd.g08.drag

import android.graphics.Path
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import edu.isel.pdm.li51xd.g08.drag.model.Draw

const val SAVED_DRAWING_KEY = "DRAG.Draw"

class DrawViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val model : Draw by lazy {
        savedState[SAVED_DRAWING_KEY] ?: Draw(Path())
    }

    fun save() {
        savedState[SAVED_DRAWING_KEY] = model
    }
}