package edu.isel.pdm.li51xd.g08.drag.model

class Repository(var game: GameState = GameState(), var config: GameConfiguration = GameConfiguration()) {
    fun reset() {
        game = GameState()
    }
}