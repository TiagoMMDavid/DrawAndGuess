package edu.isel.pdm.li51xd.g08.drag.game.remote

import android.os.Parcelable
import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlayerDrawGuess(val bookOwnerId: String, val receiverId: String, val drawGuess: DrawGuess) : Parcelable {
    fun toDto(mapper: ObjectMapper) = PlayerDrawGuessDto(bookOwnerId, receiverId, drawGuess.toDto(mapper))
}

data class PlayerDrawGuessDto(val bookOwnerId: String, val receiverId: String, val drawGuess: DrawGuessDto) {
    fun toPlayerDrawGuess(mapper: ObjectMapper) = PlayerDrawGuess(bookOwnerId, receiverId, drawGuess.toDrawGuess(mapper))
}