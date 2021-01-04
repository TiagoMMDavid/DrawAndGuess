package edu.isel.pdm.li51xd.g08.drag.game.remote

import android.os.Parcelable
import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Player(val idx: Int = 0, val id: String = UUID.randomUUID().toString(), val name: String, val book: MutableList<DrawGuess> = mutableListOf()) : Parcelable {
    fun toDto(mapper: ObjectMapper) = PlayerDto(idx, id, name, book.map { it.toDto(mapper) })

    override fun toString(): String {
        return name
    }
}

data class PlayerDto(val idx: Int, val id: String, val name: String, val book: List<DrawGuessDto>) {
    fun toPlayer(mapper: ObjectMapper) = Player(idx, id, name, book.map { it.toDrawGuess(mapper) }.toMutableList())
}