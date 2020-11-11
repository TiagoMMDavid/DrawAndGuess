package edu.isel.pdm.li51xd.g08.drag.listeners

import edu.isel.pdm.li51xd.g08.drag.model.GameState

fun interface GameListener {
    fun onStateChange(state: GameState.State)
}