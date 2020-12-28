package edu.isel.pdm.li51xd.g08.drag.game.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Player(val name: String, val book: MutableList<DrawGuess>) : Parcelable {
    fun toDto() = PlayerDto(name, book)
}

data class PlayerDto(val name: String, val book: List<DrawGuess>) {
    fun toPlayer() = Player(name, book.toMutableList())
}