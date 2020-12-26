package edu.isel.pdm.li51xd.g08.drag.lobbies.list

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import edu.isel.pdm.li51xd.g08.drag.DragApplication
import edu.isel.pdm.li51xd.g08.drag.R
import edu.isel.pdm.li51xd.g08.drag.lobbies.LobbyInfo

class DragListGamesViewModel(app: Application) : AndroidViewModel(app) {

    private val app: DragApplication by lazy { getApplication<DragApplication>()}

    val lobbies: LiveData<List<LobbyInfo>> = MutableLiveData()

    fun fetchLobbies() {
        app.repo.fetchLobbies(
            {
                (lobbies as MutableLiveData<List<LobbyInfo>>).value = it
            },
            {
                Toast.makeText(app, R.string.errorLobbies, Toast.LENGTH_LONG).show()
            }
        )
    }
}