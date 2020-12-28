package edu.isel.pdm.li51xd.g08.drag.lobbies.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.isel.pdm.li51xd.g08.drag.R
import edu.isel.pdm.li51xd.g08.drag.game.model.Player

class PlayersViewHolder(view: ViewGroup) : RecyclerView.ViewHolder(view) {

    private val playerNameView: TextView = view.findViewById(R.id.playerName)

    fun bindTo(player: Player) {
        playerNameView.text = player.name
    }
}

class PlayerListAdapter(
    private val contents: List<Player>) :
    RecyclerView.Adapter<PlayersViewHolder>() {

    override fun onBindViewHolder(holder: PlayersViewHolder, position: Int) {
        holder.bindTo(contents[position])
    }

    override fun getItemCount(): Int = contents.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayersViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.recycler_view_player, parent, false) as ViewGroup

        return PlayersViewHolder(view)
    }
}