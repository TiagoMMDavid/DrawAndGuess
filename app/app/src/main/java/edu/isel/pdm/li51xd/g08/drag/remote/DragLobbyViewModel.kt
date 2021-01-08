package edu.isel.pdm.li51xd.g08.drag.remote

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import edu.isel.pdm.li51xd.g08.drag.*
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import edu.isel.pdm.li51xd.g08.drag.remote.model.GameInfo
import edu.isel.pdm.li51xd.g08.drag.remote.model.LobbyInfo
import edu.isel.pdm.li51xd.g08.drag.utils.CountDownTimerAdapter
import edu.isel.pdm.li51xd.g08.drag.utils.runDelayed

class DragLobbyViewModel(application: Application, private val savedState: SavedStateHandle) : AndroidViewModel(application) {

    private val app: DragApplication by lazy { getApplication<DragApplication>()}

    val player: Player by lazy {
        savedState.get<Player>(PLAYER_KEY) ?: throw IllegalArgumentException()
    }
    val words: ArrayList<String> by lazy {
        savedState.get<ArrayList<String>>(WORDS_KEY) ?: throw IllegalArgumentException()
    }

    val lobbyInfo: LiveData<LobbyInfo> by lazy { MutableLiveData(savedState.get(LOBBY_INFO_KEY)!!) }
    val gameInfo: LiveData<GameInfo?> by lazy {
        val liveData = if (savedState.contains(GAME_INFO_KEY)) {
            MutableLiveData(savedState.get<GameInfo>(GAME_INFO_KEY))
        } else {
            MutableLiveData()
        }
        liveData
    }

    var timeLeft: Long = savedState[COUNTDOWN_TIME_LEFT_KEY] ?: LOBBY_COUNTDOWN_TIME
        set(value) { savedState[COUNTDOWN_TIME_LEFT_KEY] = value; field = value }

    private var timer: CountDownTimer? = null

    private val lobbySubscription = app.repo.subscribeToLobby(
        lobbyInfo.value!!.id,
        onSubscriptionError = { (lobbyInfo as MutableLiveData<LobbyInfo>).value = null },
        onStateChange = { (lobbyInfo as MutableLiveData<LobbyInfo>).value = it },
    )
    private val gameSubscription = app.repo.subscribeToGame(
        lobbyInfo.value!!.id,
        onSubscriptionError = {
            savedState[GAME_INFO_KEY] = null
            (gameInfo as MutableLiveData<GameInfo?>).value = null
        },
        onStateChange = {
            clearSubscriptions()
            savedState[GAME_INFO_KEY] = it
            (gameInfo as MutableLiveData<GameInfo?>).value = it
        }
    )

    private var isScheduled: Boolean = false

    override fun onCleared() {
        timer?.cancel()
        clearSubscriptions()
    }

    fun scheduleWork(millis: Long, work: () -> Unit) {
        if (!isScheduled) {
            isScheduled = true
            runDelayed(millis) {
                work()
            }
        }
    }

    fun startTimer(onTickListener: (Long) -> Unit) {
        timer?.cancel()

        timer = CountDownTimerAdapter(timeLeft, COUNTDOWN_INTERVAL) { millisUntilFinished ->
            timeLeft = millisUntilFinished
            onTickListener(millisUntilFinished)
        }
        timer?.start()
    }

    fun clearSubscriptions() {
        lobbySubscription.remove()
        gameSubscription.remove()
    }

    fun exitLobby() {
        app.repo.exitLobby(lobbyInfo.value!!.id, player)
    }
}