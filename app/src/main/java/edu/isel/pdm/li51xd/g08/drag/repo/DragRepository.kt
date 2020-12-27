package edu.isel.pdm.li51xd.g08.drag.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.RequestQueue
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.isel.pdm.li51xd.g08.drag.BuildConfig
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.lobbies.LobbyInfo
import java.lang.IllegalStateException

private const val LOBBIES_COLLECTION = "lobbies"
private const val GAMES_COLLECTION = "games"

private const val LOBBY_NAME = "name"
private const val LOBBY_PLAYER_NAMES = "playerNames"
private const val LOBBY_GAME_CONFIG = "gameConfig"

private const val GAME_CONFIG_PLAYER_COUNT = "playerCount"
private const val GAME_CONFIG_ROUND_COUNT = "roundCount"

private fun toGameConfiguration(map: Map<String, Any>) =
    GameConfiguration(
        (map[GAME_CONFIG_PLAYER_COUNT] as Long).toInt(),
        (map[GAME_CONFIG_ROUND_COUNT] as Long).toInt()
    )

private fun DocumentSnapshot.toLobbyInfo() =
    LobbyInfo(
        id,
        data!![LOBBY_NAME] as String,
        data!![LOBBY_PLAYER_NAMES] as MutableList<String>,
        toGameConfiguration(data!![LOBBY_GAME_CONFIG] as Map<String, Any>)
    )

class DragRepository(private val queue: RequestQueue, private val mapper: ObjectMapper) {

    fun fetchRandomWords(limit: Int) : LiveData<Result<List<String>>> {
        val result = MutableLiveData<Result<List<String>>>()
        val request = GetRandomWordsRequest(
            "${RANDOM_WORDS_URL}?hasDictionaryDef=true&includePartOfSpeech=noun&minDictionaryCount=20&minCorpusCount=80&limit=${limit}&api_key=${BuildConfig.wordnikApiKey}",
            mapper,
            {
                result.value = Result.success(modelFromDto(it))
            },
            {
                result.value = Result.failure(it)
            }
        )

        queue.add(request)
        return result
    }

    fun fetchLobbies(onSuccess: (List<LobbyInfo>) -> Unit, onError: (Exception) -> Unit) {
        Firebase.firestore.collection(LOBBIES_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                onSuccess(result.map { it.toLobbyInfo() }.toList())
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    fun createLobby(lobbyName: String, playerName: String, gameConfiguration: GameConfiguration,
                    onSuccess: (LobbyInfo) -> Unit,
                    onError: (Exception) -> Unit) {

        val playerNames = mutableListOf(playerName)
        Firebase.firestore.collection(LOBBIES_COLLECTION)
            .add(hashMapOf(LOBBY_NAME to lobbyName, LOBBY_PLAYER_NAMES to playerNames, LOBBY_GAME_CONFIG to gameConfiguration))
            .addOnSuccessListener { onSuccess(LobbyInfo(it.id, lobbyName, playerNames, gameConfiguration)) }
            .addOnFailureListener { onError(it) }
    }

    fun tryJoinLobby(lobbyId: String, playerName: String,
                     onSuccess: (LobbyInfo) -> Unit,
                     onError: (Exception) -> Unit) {

        val document = Firebase.firestore
            .collection(LOBBIES_COLLECTION)
            .document(lobbyId)

        // TODO: Deal with concurrency
        document
            .get()
            .addOnSuccessListener {
                val lobby = it.toLobbyInfo()
                if (lobby.playerNames.size < lobby.gameConfig.playerCount) {
                    lobby.playerNames.add(playerName)
                    document
                        .update(LOBBY_PLAYER_NAMES, lobby.playerNames)
                        .addOnSuccessListener { onSuccess(lobby) }
                        .addOnFailureListener { err -> onError(err) }
                } else {
                    onError(IllegalStateException("Lobby is already full"))
                }
            }
            .addOnFailureListener { onError(it) }
    }

    fun deleteLobby(lobbyId: String,
                    onSuccess: () -> Unit,
                    onError: (Exception) -> Unit) {
        Firebase.firestore
            .collection(LOBBIES_COLLECTION)
            .document(lobbyId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }
}