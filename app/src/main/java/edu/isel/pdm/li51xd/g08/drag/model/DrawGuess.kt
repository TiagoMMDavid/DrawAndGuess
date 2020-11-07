package edu.isel.pdm.li51xd.g08.drag.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DrawGuess(val drawing: Drawing, val word: String) : Parcelable