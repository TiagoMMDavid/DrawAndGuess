package edu.isel.pdm.li51xd.g08.drag.game.model

import android.os.Parcelable
import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.remote.model.PlayerDto
import java.util.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class Player(val idx: Int = 0, val id: String = UUID.randomUUID().toString(), val name: String, val book: MutableList<DrawGuess> = mutableListOf()) : Parcelable {
    fun toDto(mapper: ObjectMapper) = PlayerDto(idx, id, name, book.map { it.toDto(mapper) })

    override fun toString(): String {
        return name
    }
}