package edu.isel.pdm.li51xd.g08.drag.lobbies.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.isel.pdm.li51xd.g08.drag.DragConfigureActivity
import edu.isel.pdm.li51xd.g08.drag.R
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityListBinding
import edu.isel.pdm.li51xd.g08.drag.game.model.GAME_MODE_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.LOBBY_INFO_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode
import edu.isel.pdm.li51xd.g08.drag.game.model.PLAYER_KEY
import edu.isel.pdm.li51xd.g08.drag.game.model.PLAYER_NAME_KEY
import edu.isel.pdm.li51xd.g08.drag.lobbies.DragLobbyActivity
import edu.isel.pdm.li51xd.g08.drag.lobbies.list.view.GamesListAdapter
import edu.isel.pdm.li51xd.g08.drag.repo.WORDS_KEY
import edu.isel.pdm.li51xd.g08.drag.utils.EditTextNoEnter

class DragListGamesActivity : AppCompatActivity() {
    private val binding: ActivityListBinding by lazy { ActivityListBinding.inflate(layoutInflater) }

    private val viewModel: DragListGamesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.lobbiesList.setHasFixedSize(true)
        binding.lobbiesList.layoutManager = LinearLayoutManager(this)
        binding.playerName.addTextChangedListener(EditTextNoEnter())

        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = true
            viewModel.fetchLobbies()
        }

        binding.createGameButton.setOnClickListener {
            if (binding.playerName.text.isBlank()) {
                Toast.makeText(this, R.string.errorNoPlayerName, Toast.LENGTH_LONG).show()
            } else {
                startActivity(Intent(this, DragConfigureActivity::class.java).apply {
                    putExtra(GAME_MODE_KEY, Mode.ONLINE.name)
                    putExtra(PLAYER_NAME_KEY, binding.playerName.text.toString())
                })
            }
        }

        viewModel.lobbies.observe(this) {
            binding.lobbiesList.adapter = GamesListAdapter(it) { lobby ->
                if (binding.playerName.text.isBlank()) {
                    Toast.makeText(this, R.string.errorNoPlayerName, Toast.LENGTH_LONG).show()
                } else if (binding.createGameButton.isEnabled) {
                    binding.createGameButton.isEnabled = false
                    binding.refreshLayout.isEnabled = false
                    binding.loadingLobby.visibility = VISIBLE
                    viewModel.tryJoinLobby(lobby.id, binding.playerName.text.toString(), lobby.gameConfig.roundCount)
                }
            }
            binding.refreshLayout.isRefreshing = false
        }

        viewModel.joinedLobby.observe(this) { lobby ->
            if (lobby != null) {
                startActivity(Intent(this, DragLobbyActivity::class.java).apply {
                    putExtra(LOBBY_INFO_KEY, lobby)
                    putStringArrayListExtra(WORDS_KEY, viewModel.words)
                    putExtra(PLAYER_KEY, viewModel.player)
                })
            }
            binding.createGameButton.isEnabled = true
            binding.refreshLayout.isEnabled = true
            binding.loadingLobby.visibility = GONE
        }
    }

    override fun onResume() {
        super.onResume()
        binding.refreshLayout.isRefreshing = true
        viewModel.fetchLobbies()
    }
}