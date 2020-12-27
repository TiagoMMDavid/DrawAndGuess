package edu.isel.pdm.li51xd.g08.drag.lobbies

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityLobbyBinding
import edu.isel.pdm.li51xd.g08.drag.game.model.LOBBY_INFO_KEY
import edu.isel.pdm.li51xd.g08.drag.lobbies.list.DragListGamesViewModel
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY

class DragLobbyActivity : AppCompatActivity() {

    private val binding: ActivityLobbyBinding by lazy { ActivityLobbyBinding.inflate(layoutInflater) }

    private val lobbyInfo: LobbyInfo by lazy {
        intent.getParcelableExtra<LobbyInfo>(LOBBY_INFO_KEY) ?: throw IllegalArgumentException()
    }

    private val words: ArrayList<String> by lazy {
        intent.getStringArrayListExtra(WORDS_KEY) ?: throw IllegalArgumentException()
    }

    private val viewModel: DragListGamesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}