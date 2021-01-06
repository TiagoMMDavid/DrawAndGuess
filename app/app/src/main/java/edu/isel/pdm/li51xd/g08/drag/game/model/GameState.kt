package edu.isel.pdm.li51xd.g08.drag.game.model

import android.os.Parcelable
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState.State.DEFINING
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameState(val currentDrawing: Drawing = Drawing(),
                     var playCount: Int = 0,
                     var currRound: Int = 1,
                     var state: State = DEFINING) : Parcelable {

    /**
     * DEFINING - Defining word
     * DRAWING - Drawing
     * GUESSING - Guessing a drawing
     * RESULTS - Shows every drawing and guess for a round
     */
    enum class State { DEFINING, DRAWING, GUESSING, RESULTS }
}