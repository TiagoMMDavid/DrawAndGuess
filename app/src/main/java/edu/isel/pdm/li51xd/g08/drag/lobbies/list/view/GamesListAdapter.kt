package edu.isel.pdm.li51xd.g08.drag.lobbies.list.view

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.isel.pdm.li51xd.g08.drag.R
import edu.isel.pdm.li51xd.g08.drag.game.remote.LobbyInfo

class GameViewHolder(private val view: ViewGroup) : RecyclerView.ViewHolder(view) {

    private val lobbyNameView: TextView = view.findViewById(R.id.lobbyName)
    private val lobbyInfoView: TextView = view.findViewById(R.id.lobbyInfo)

    fun bindTo(lobby: LobbyInfo, itemSelectedListener: (LobbyInfo) -> Unit) {
        val config = lobby.gameConfig
        lobbyNameView.text = lobby.name
        lobbyInfoView.text = itemView.context.getString(R.string.lobbyInfo, lobby.players.size, config.playerCount, config.roundCount)

        view.setOnClickListener {
            itemSelectedListener(lobby)
        }
    }
}

class GamesListAdapter(
        private val contents: List<LobbyInfo>,
        private val itemSelectedListener: (LobbyInfo) -> Unit) :
    RecyclerView.Adapter<GameViewHolder>() {

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bindTo(contents[position], itemSelectedListener)
    }

    override fun getItemCount(): Int = contents.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.recycler_view_lobby, parent, false) as ViewGroup

        return GameViewHolder(view)
    }
}