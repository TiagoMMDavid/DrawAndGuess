package edu.isel.pdm.li51xd.g08.drag.game.model

import android.os.Parcelable
import edu.isel.pdm.li51xd.g08.drag.game.model.GameState.State.DRAWING
import java.util.*
import kotlinx.android.parcel.Parcelize

const val GAME_MODE_KEY = "DRAG.GameMode"
const val PLAYER_NAME_KEY = "DRAG.PlayerName"
const val PLAYER_KEY = "DRAG.Player"
const val GAME_CONFIGURATION_KEY = "DRAG.GameConfiguration"
const val LOBBY_INFO_KEY = "DRAG.LobbyInfo"
const val GAME_STATE_KEY = "DRAG.GameState"

const val MIN_PLAYERS = 5
const val MAX_PLAYERS = 10

const val MIN_ROUNDS = 1
const val MAX_ROUNDS = 10

@Parcelize
data class GameConfiguration(val playerCount: Int = MIN_PLAYERS,
                             val roundCount: Int = MIN_ROUNDS) : Parcelable

enum class Mode {
    ONLINE,
    OFFLINE
}

@Parcelize
data class GameState(var currentDrawing: Drawing = Drawing(),
                     val drawGuesses: LinkedList<DrawGuess> = LinkedList(),
                     var currRound: Int = 1,
                     var state: State = DRAWING) : Parcelable {

    /**
     * DEFINING - Defining word
     * DRAWING - Drawing
     * GUESSING - Guessing a drawing
     * RESULTS - Shows every drawing and guess for a round
     */
    enum class State { DRAWING, GUESSING, RESULTS }
}