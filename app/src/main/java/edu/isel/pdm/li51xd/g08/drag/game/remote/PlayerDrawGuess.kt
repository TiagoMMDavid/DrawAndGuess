package edu.isel.pdm.li51xd.g08.drag.game.remote

import android.os.Parcelable
import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType.*
import edu.isel.pdm.li51xd.g08.drag.game.model.Drawing
import edu.isel.pdm.li51xd.g08.drag.game.model.Vector
import edu.isel.pdm.li51xd.g08.drag.game.model.Word
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class PlayerDrawGuess (val receiverId: String, val drawGuess: DrawGuess) : Parcelable {
    fun toDto(mapper: ObjectMapper): PlayerDrawGuessDto {
        when(val type = drawGuess.getType()) {
            DRAWING -> {
                val drawing = drawGuess as Drawing
                return PlayerDrawGuessDto(
                        receiverId,
                        type.name,
                        null,
                        drawing.vectors.map { mapper.writeValueAsString(it) }
                )
            }
            WORD -> {
                val word = drawGuess as Word
                return PlayerDrawGuessDto(
                        receiverId,
                        type.name,
                        word.word,
                        null
                )
            }
        }
    }
}

data class PlayerDrawGuessDto(val receiverId: String, val type: String, val word: String?, val drawing: List<String>?) {
    fun toPlayerDrawGuess(mapper: ObjectMapper): PlayerDrawGuess {
        when(val type = DrawGuessType.valueOf(this.type)) {
            DRAWING -> {
                val vectors = LinkedList<Vector>()
                drawing!!.forEach {
                    vectors.add(mapper.readValue(it, Vector::class.java))
                }
                return PlayerDrawGuess(
                        receiverId,
                        Drawing(vectors)
                )
            }
            WORD -> {
                return PlayerDrawGuess(
                        receiverId,
                        Word(word!!)
                )
            }
        }
    }
}