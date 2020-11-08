package edu.isel.pdm.li51xd.g08.drag.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Point(val x: Float, val y: Float) : Parcelable

@Parcelize
data class Vector(val points: LinkedList<Point> = LinkedList()) : Parcelable

@Parcelize
data class Drawing(var currVector: Vector = Vector(),
                   val vectors: LinkedList<Vector> = LinkedList(),
                   var sizeX: Float = 0f,
                   var sizeY: Float = 0f) : Parcelable