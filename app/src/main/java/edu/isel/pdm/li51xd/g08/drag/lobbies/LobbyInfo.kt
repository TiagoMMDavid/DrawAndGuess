package edu.isel.pdm.li51xd.g08.drag.lobbies

import android.os.Parcelable
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LobbyInfo(
    val id: String,
    val name: String,
    val playerNames: List<String>,
    val gameConfig: GameConfiguration
) : Parcelable