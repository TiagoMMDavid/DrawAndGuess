package edu.isel.pdm.li51xd.g08.drag.game.model

import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.ResultType
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.ResultType.WORD
import kotlinx.android.parcel.Parcelize

@Parcelize
class Word(val word: String) : DrawGuess {
    override fun getResultType(): ResultType {
        return WORD
    }
}