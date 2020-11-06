package edu.isel.pdm.li51xd.g08.drag.model

import android.graphics.Path
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class Draw(val path : @RawValue Path) : Parcelable