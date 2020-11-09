package edu.isel.pdm.li51xd.g08.drag.model

import android.os.Parcelable
import edu.isel.pdm.li51xd.g08.drag.model.DrawGuess.ResultType.DRAWING
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Point(val x: Float, val y: Float) : Parcelable

@Parcelize
data class Vector(val points: ArrayList<Point> = ArrayList()) : Parcelable

@Parcelize
class Drawing(val vectors: LinkedList<Vector> = LinkedList()) : DrawGuess {

    override fun getResultType(): DrawGuess.ResultType {
        return DRAWING
    }
}