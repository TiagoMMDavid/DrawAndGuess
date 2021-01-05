package edu.isel.pdm.li51xd.g08.drag.game.model

import android.os.Parcelable
import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.remote.model.DrawGuessDto

interface DrawGuess : Parcelable {
    enum class DrawGuessType { DRAWING, WORD }

    fun getType() : DrawGuessType
    fun toDto(mapper: ObjectMapper) : DrawGuessDto
}