package edu.isel.pdm.li51xd.g08.drag.remote

import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.isel.pdm.li51xd.g08.drag.*
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityListBinding
import edu.isel.pdm.li51xd.g08.drag.game.DragConfigureActivity
import edu.isel.pdm.li51xd.g08.drag.game.model.Mode
import edu.isel.pdm.li51xd.g08.drag.remote.view.GamesListAdapter
import edu.isel.pdm.li51xd.g08.drag.utils.EditTextNoEnter

class DragListLobbiesActivity : AppCompatActivity() {
    private val binding: ActivityListBinding by lazy { ActivityListBinding.inflate(layoutInflater) }

    private val viewModel: DragListLobbiesViewModel by viewModels()

    private fun refreshLobbyList() {
        binding.refreshLayout.isRefreshing = true
        viewModel.fetchLobbies()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.lobbiesList.setHasFixedSize(true)
        binding.lobbiesList.layoutManager = LinearLayoutManager(this)
        binding.playerName.addTextChangedListener(EditTextNoEnter())

        binding.refreshLayout.setOnRefreshListener {
            refreshLobbyList()
        }

        binding.createGameButton.setOnClickListener {
            if (binding.playerName.text.isBlank()) {
                Toast.makeText(this, R.string.errorNoPlayerName, Toast.LENGTH_LONG).show()
            } else {
                binding.createGameButton.isEnabled = false
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
                    putExtra(PLAYER_KEY, viewModel.player)
                    putStringArrayListExtra(WORDS_KEY, viewModel.words)
                })
                viewModel.finishJoinLobby()
            } else {
                refreshLobbyList()
            }
            binding.createGameButton.isEnabled = true
            binding.refreshLayout.isEnabled = true
            binding.loadingLobby.visibility = GONE
        }
    }

    override fun onResume() {
        super.onResume()
        binding.createGameButton.isEnabled = true
        refreshLobbyList()
    }
}