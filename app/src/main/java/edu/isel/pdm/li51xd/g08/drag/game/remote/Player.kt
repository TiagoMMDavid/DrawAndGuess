package edu.isel.pdm.li51xd.g08.drag.game.remote

import android.os.Parcelable
import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Player(val name: String, val book: MutableList<DrawGuess>, val id: String = UUID.randomUUID().toString()) : Parcelable {
    fun toDto(mapper: ObjectMapper) = PlayerDto(id, name, book.map { it.toDto(mapper) })
}

data class PlayerDto(val id: String, val name: String, val book: List<DrawGuessDto>) {
    fun toPlayer(mapper: ObjectMapper) = Player(name, book.map { it.toDrawGuess(mapper) }.toMutableList(), id)
}