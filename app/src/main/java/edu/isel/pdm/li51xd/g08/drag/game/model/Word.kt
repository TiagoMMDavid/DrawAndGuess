package edu.isel.pdm.li51xd.g08.drag.game.model

import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType.WORD
import kotlinx.android.parcel.Parcelize

@Parcelize
class Word(val word: String) : DrawGuess {
    override fun getType(): DrawGuessType {
        return WORD
    }
}