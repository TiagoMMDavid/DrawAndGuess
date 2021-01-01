package edu.isel.pdm.li51xd.g08.drag.game.model

import android.os.Parcelable
import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType.DRAWING
import edu.isel.pdm.li51xd.g08.drag.game.remote.DrawGuessDto
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.collections.ArrayList

@Parcelize
data class Point(val x: Float, val y: Float) : Parcelable

@Parcelize
data class Vector(val points: ArrayList<Point> = ArrayList()) : Parcelable

@Parcelize
class Drawing(val vectors: LinkedList<Vector> = LinkedList()) : DrawGuess {
    override fun getType(): DrawGuessType {
        return DRAWING
    }

    override fun toDto(mapper: ObjectMapper) =
        DrawGuessDto(getType().name, null, vectors.map { mapper.writeValueAsString(it) })

    override fun equals(other: Any?): Boolean {
        val otherDrawGuess = (other as DrawGuess)
        if (getType() == otherDrawGuess.getType()) {
            return this.vectors == (other as Drawing).vectors
        }
        return false
    }
}