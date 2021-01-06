package edu.isel.pdm.li51xd.g08.drag.remote.model

import android.os.Parcelable
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameInfo (
    val id: String,
    val players: Map<String, Player>
) : Parcelable