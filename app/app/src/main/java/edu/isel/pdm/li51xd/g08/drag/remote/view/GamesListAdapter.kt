package edu.isel.pdm.li51xd.g08.drag.remote.view

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import edu.isel.pdm.li51xd.g08.drag.R
import edu.isel.pdm.li51xd.g08.drag.R.color
import edu.isel.pdm.li51xd.g08.drag.R.string
import edu.isel.pdm.li51xd.g08.drag.remote.model.LobbyInfo

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

    fun emptyBind() {
        (lobbyNameView.parent as LinearLayout).gravity = Gravity.CENTER
        lobbyNameView.text = itemView.context.getString(string.lobbyListNoLobbies)
        lobbyInfoView.text = itemView.context.getString(string.lobbyListSwipeDown)
        view.isClickable = false
        view.isFocusable = false
        view.setBackgroundColor(ResourcesCompat.getColor(itemView.context.resources, color.colorLobbyListBg, null))
    }
}

class GamesListAdapter(
    private val contents: List<LobbyInfo>,
    private val itemSelectedListener: (LobbyInfo) -> Unit) :
    RecyclerView.Adapter<GameViewHolder>() {

    private val isEmpty = contents.isEmpty()

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        if (isEmpty) {
            holder.emptyBind()
        } else {
            holder.bindTo(contents[position], itemSelectedListener)
        }
    }

    override fun getItemCount(): Int = if (isEmpty) 1 else contents.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.recycler_view_lobby, parent, false) as ViewGroup

        return GameViewHolder(view)
    }
}