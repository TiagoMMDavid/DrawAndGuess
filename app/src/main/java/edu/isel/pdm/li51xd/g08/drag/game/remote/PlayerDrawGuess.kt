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
import java.lang.IllegalArgumentException
import java.util.*

@Parcelize
data class PlayerDrawGuess(val bookOwnerId: String, val receiverId: String, val drawGuess: DrawGuess) : Parcelable {
    fun toDto(mapper: ObjectMapper): PlayerDrawGuessDto {
        when(val type = drawGuess.getType()) {
            DRAWING -> {
                val drawing = drawGuess as Drawing
                return PlayerDrawGuessDto(bookOwnerId, receiverId, type.name,
                        null, drawing.vectors.map { mapper.writeValueAsString(it) })
            }
            WORD -> {
                val word = drawGuess as Word
                return PlayerDrawGuessDto(bookOwnerId, receiverId, type.name,
                        word.word, null)
            }
        }
    }
}

data class PlayerDrawGuessDto(val bookOwnerId: String, val receiverId: String, val type: String, val word: String?, val drawing: List<String>?) {
    fun toPlayerDrawGuess(mapper: ObjectMapper): PlayerDrawGuess {
        when(DrawGuessType.valueOf(this.type)) {
            DRAWING -> {
                val vectors = LinkedList<Vector>()
                drawing!!.forEach { vectors.add(mapper.readValue(it, Vector::class.java)) }
                return PlayerDrawGuess(bookOwnerId, receiverId, Drawing(vectors))
            }
            WORD -> return PlayerDrawGuess(bookOwnerId, receiverId, Word(word!!))
        }
    }
}