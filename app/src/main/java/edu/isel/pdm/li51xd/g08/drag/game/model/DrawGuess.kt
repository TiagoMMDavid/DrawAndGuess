package edu.isel.pdm.li51xd.g08.drag.game.model

import android.os.Parcelable

interface DrawGuess : Parcelable {
    enum class DrawGuessType { DRAWING, WORD }

    fun getType() : DrawGuessType
}