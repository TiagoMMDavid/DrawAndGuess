package edu.isel.pdm.li51xd.g08.drag.model

import android.os.Parcelable
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.*
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class GameState(
        val currentDrawing: Drawing = Drawing(),
        val drawGuesses: LinkedList<DrawGuess> = LinkedList(),
        var state: State = DEFINING) : Parcelable {
            enum class State { DEFINING, DRAWING, GUESSING, RESULTS
        }
}