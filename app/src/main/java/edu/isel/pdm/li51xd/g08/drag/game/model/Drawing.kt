package edu.isel.pdm.li51xd.g08.drag.game.model

import android.os.Parcelable
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.ResultType
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess.ResultType.DRAWING
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Point(val x: Float, val y: Float) : Parcelable

@Parcelize
data class Vector(val points: ArrayList<Point> = ArrayList()) : Parcelable

@Parcelize
class Drawing(val vectors: LinkedList<Vector> = LinkedList()) : DrawGuess {
    override fun getResultType(): ResultType {
        return DRAWING
    }
}