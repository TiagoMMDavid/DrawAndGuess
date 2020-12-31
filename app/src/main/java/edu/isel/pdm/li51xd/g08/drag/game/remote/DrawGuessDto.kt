package edu.isel.pdm.li51xd.g08.drag.game.remote

import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType.DRAWING
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType.WORD
import edu.isel.pdm.li51xd.g08.drag.game.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.game.model.Vector
import edu.isel.pdm.li51xd.g08.drag.game.model.Word
import java.util.*

data class DrawGuessDto(val type: String, val word: String?, val drawing: List<String>?) {
    fun toDrawGuess(mapper: ObjectMapper): DrawGuess {
        when(DrawGuessType.valueOf(this.type)) {
            DRAWING -> {
                val vectors = LinkedList<Vector>()
                drawing!!.forEach { vectors.add(mapper.readValue(it, Vector::class.java)) }
                return Drawing(vectors)
            }
            WORD -> return Word(word!!)
        }
    }
}