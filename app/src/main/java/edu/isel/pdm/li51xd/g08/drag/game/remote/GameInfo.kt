package edu.isel.pdm.li51xd.g08.drag.game.remote

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GameInfo (
    val id: String,
    val players: Map<String, Player>
) : Parcelable