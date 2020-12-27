package edu.isel.pdm.li51xd.g08.drag.lobbies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import edu.isel.pdm.li51xd.g08.drag.DragApplication

class DragLobbyViewModel(app: Application) : AndroidViewModel(app) {

    private val app: DragApplication by lazy { getApplication<DragApplication>()}
}