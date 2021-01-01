package edu.isel.pdm.li51xd.g08.drag.game.model

import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType.WORD
import edu.isel.pdm.li51xd.g08.drag.game.remote.DrawGuessDto
import kotlinx.android.parcel.Parcelize

@Parcelize
class Word(val word: String) : DrawGuess {
    override fun getType(): DrawGuessType {
        return WORD
    }

    override fun toDto(mapper: ObjectMapper) = DrawGuessDto(getType().name, word, null)

    override fun equals(other: Any?): Boolean {
        val otherDrawGuess = (other as DrawGuess)
        if (getType() == otherDrawGuess.getType()) {
            return this.word == (other as Word).word
        }
        return false
    }
}