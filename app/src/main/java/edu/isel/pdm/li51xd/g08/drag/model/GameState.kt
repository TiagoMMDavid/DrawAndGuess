package edu.isel.pdm.li51xd.g08.drag.model

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import edu.isel.pdm.li51xd.g08.drag.model.GameConfiguration.Mode.*
import edu.isel.pdm.li51xd.g08.drag.model.GameState.State.LAUNCHER
import java.util.LinkedList
import kotlinx.android.parcel.Parcelize

const val MIN_PLAYERS = 5
const val MAX_PLAYERS = 10

const val MIN_ROUNDS = 1
const val MAX_ROUNDS = 10

@Parcelize
data class GameConfiguration(val playerCount: Int = MIN_PLAYERS,
                             val roundCount: Int = MIN_ROUNDS,
                             val mode: Mode = OFFLINE) : Parcelable {
    enum class Mode { ONLINE, OFFLINE }
}

@Parcelize
data class GameState(var currentDrawing: Drawing = Drawing(),
                     val drawGuesses: LinkedList<DrawGuess> = LinkedList(),
                     var currRound: Int = 1,
                     var state: State = LAUNCHER) : Parcelable {

    /**
     * LAUNCHER - Initial launcher
     * CONFIGURE - Selecting number of rounds and players
     * LIST - List available games
     * LOBBY - Waiting for players
     * DEFINING - Defining word
     * DRAWING - Drawing
     * GUESSING - Guessing a drawing
     * RESULTS - Shows every drawing and guess for a round
     */
    enum class State { LAUNCHER, CONFIGURE, LIST, LOBBY, DEFINING, DRAWING, GUESSING, RESULTS }
}