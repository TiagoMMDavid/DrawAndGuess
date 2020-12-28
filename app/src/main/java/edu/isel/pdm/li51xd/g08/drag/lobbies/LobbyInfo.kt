package edu.isel.pdm.li51xd.g08.drag.lobbies

import android.os.Parcelable
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LobbyInfo(
    val id: String,
    val name: String,
    val players: MutableList<Player>,
    val gameConfig: GameConfiguration
) : Parcelable