package edu.isel.pdm.li51xd.g08.drag.game.model

import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType.WORD
import edu.isel.pdm.li51xd.g08.drag.remote.model.DrawGuessDto
import kotlinx.parcelize.Parcelize

@Parcelize
class Word(val word: String) : DrawGuess {
    override fun getType(): DrawGuessType {
        return WORD
    }

    override fun toDto(mapper: ObjectMapper) = DrawGuessDto(getType().name, word, null)
}