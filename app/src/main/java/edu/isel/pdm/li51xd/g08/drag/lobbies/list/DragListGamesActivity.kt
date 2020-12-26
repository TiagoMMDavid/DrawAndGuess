package edu.isel.pdm.li51xd.g08.drag.lobbies.list

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.R
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityDrawBinding
import edu.isel.pdm.li51xd.g08.drag.databinding.ActivityListBinding
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.lobbies.list.view.GamesListAdapter

class DragListGamesActivity : AppCompatActivity() {
    private val binding: ActivityListBinding by lazy { ActivityListBinding.inflate(layoutInflater) }

    private val viewModel: DragListGamesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.lobbiesList.setHasFixedSize(true)
        binding.lobbiesList.layoutManager = LinearLayoutManager(this)

        // Get view model instance and add its contents to the recycler view
        viewModel.lobbies.observe(this) {
            binding.lobbiesList.adapter = GamesListAdapter(it) {
                Toast.makeText(this, "Select lobby ${it.name}", Toast.LENGTH_LONG).show()
            }
            binding.refreshLayout.isRefreshing = false
        }

        // Setup ui event handlers
        binding.refreshLayout.setOnRefreshListener {
            binding.refreshLayout.isRefreshing = true
            viewModel.fetchLobbies()
        }

        // Setup ui event handlers
        binding.createGameButton.setOnClickListener {
            (application as DragApplication).repo.createLobby("Example", "Player", GameConfiguration(),
                {
                    Toast.makeText(this, "Created ${it.name}", Toast.LENGTH_LONG).show()
                },
                {
                    Toast.makeText(this, "Error creating lobby", Toast.LENGTH_LONG).show()
                })
        }
    }
}