package edu.isel.pdm.li51xd.g08.drag.model

import android.os.Parcelable
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.LAUNCHER
import java.util.LinkedList
import kotlinx.android.parcel.Parcelize

const val MIN_PLAYERS = 5
const val MAX_PLAYERS = 10

const val MIN_ROUNDS = 1
const val MAX_ROUNDS = 10

@Parcelize
data class GameConfiguration(val playerCount: Int = MIN_PLAYERS, val roundCount: Int = MIN_ROUNDS) : Parcelable

@Parcelize
data class GameState(var currentDrawing: Drawing = Drawing(),
                     val drawGuesses: LinkedList<DrawGuess> = LinkedList(),
                     var currRound: Int = 1,
                     var state: State = LAUNCHER) : Parcelable {

        enum class State { LAUNCHER, DEFINING, DRAWING, GUESSING, RESULTS }
}