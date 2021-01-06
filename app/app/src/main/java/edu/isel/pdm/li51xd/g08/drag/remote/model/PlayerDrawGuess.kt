package edu.isel.pdm.li51xd.g08.drag.remote.model

import android.os.Parcelable
import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerDrawGuess(val bookOwnerId: String, val drawGuess: DrawGuess) : Parcelable {
    fun toDto(mapper: ObjectMapper) = PlayerDrawGuessDto(bookOwnerId, drawGuess.toDto(mapper))
}

data class PlayerDrawGuessDto(val bookOwnerId: String, val drawGuess: DrawGuessDto) {
    fun toPlayerDrawGuess(mapper: ObjectMapper) = PlayerDrawGuess(bookOwnerId, drawGuess.toDrawGuess(mapper))
}