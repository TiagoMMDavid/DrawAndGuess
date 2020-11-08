package edu.isel.pdm.li51xd.g08.drag.model

import edu.isel.pdm.li51xd.g08.drag.model.DrawGuess.ResultType.WORD
import kotlinx.android.parcel.Parcelize

@Parcelize
class Word(val word: String) : DrawGuess {
    override fun getResultType(): DrawGuess.ResultType {
        return WORD
    }
}