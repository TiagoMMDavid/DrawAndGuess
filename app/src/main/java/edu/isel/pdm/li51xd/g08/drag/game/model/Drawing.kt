package edu.isel.pdm.li51xd.g08.drag.game.model

import android.os.Parcelable
import com.fasterxml.jackson.databind.ObjectMapper
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.DrawGuessType.DRAWING
import edu.isel.pdm.li51xd.g08.drag.remote.model.DrawGuessDto
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Point(val x: Float, val y: Float) : Parcelable

@Parcelize
data class Vector(val points: ArrayList<Point> = ArrayList()) : Parcelable

@Parcelize
class Drawing(val vectors: MutableList<Vector> = mutableListOf()) : DrawGuess {
    fun copy() = Drawing(vectors.toMutableList())

    fun clear() {
        vectors.clear()
    }

    override fun getType(): DrawGuessType {
        return DRAWING
    }

    override fun toDto(mapper: ObjectMapper) =
        DrawGuessDto(getType().name, null, vectors.map { mapper.writeValueAsString(it) })
}