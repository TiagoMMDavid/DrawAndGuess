package edu.isel.pdm.li51xd.g08.drag.game.model

import android.os.Parcelable
import edu.isel.pdm.li51xd.g08.drag.MIN_PLAYERS
import edu.isel.pdm.li51xd.g08.drag.MIN_ROUNDS
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GameConfiguration(val playerCount: Int = MIN_PLAYERS,
                             val roundCount: Int = MIN_ROUNDS
) : Parcelable

enum class Mode {
    ONLINE,
    OFFLINE
}