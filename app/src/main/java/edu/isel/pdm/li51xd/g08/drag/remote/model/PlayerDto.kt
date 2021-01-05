package edu.isel.pdm.li51xd.g08.drag.remote.model

import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.game.model.Player

data class PlayerDto(val idx: Int, val id: String, val name: String, val book: List<DrawGuessDto>) {
    fun toPlayer(mapper: ObjectMapper) = Player(idx, id, name, book.map { it.toDrawGuess(mapper) }.toMutableList())
}