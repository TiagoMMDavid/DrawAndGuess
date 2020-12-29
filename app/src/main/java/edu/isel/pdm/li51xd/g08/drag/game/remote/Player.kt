package edu.isel.pdm.li51xd.g08.drag.game.remote

import android.os.Parcelable
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import java.util.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Player(val name: String, val book: MutableList<DrawGuess>, val id: String = UUID.randomUUID().toString()) : Parcelable {
    fun toDto() = PlayerDto(id, name, book)
}

data class PlayerDto(val id: String, val name: String, val book: List<DrawGuess>) {
    fun toPlayer() = Player(name, book.toMutableList(), id)
}