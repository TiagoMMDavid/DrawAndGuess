package edu.isel.pdm.li51xd.g08.drag.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.RequestQueue
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.isel.pdm.li51xd.g08.drag.BuildConfig
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.game.model.Player
import edu.isel.pdm.li51xd.g08.drag.game.model.PlayerDto
import edu.isel.pdm.li51xd.g08.drag.lobbies.LobbyInfo

private const val LOBBIES_COLLECTION = "lobbies"
private const val GAMES_COLLECTION = "games"

private const val LOBBY_NAME = "name"
private const val LOBBY_PLAYERS = "players"
private const val LOBBY_GAME_CONFIG = "gameConfig"

private const val GAME_CONFIG_PLAYER_COUNT = "playerCount"
private const val GAME_CONFIG_ROUND_COUNT = "roundCount"

private const val GAME_PLAYERS = "players"
private const val GAME_DRAW_GUESSES = "drawGuesses"

private fun toGameConfiguration(map: Map<String, Any>) =
    GameConfiguration(
        (map[GAME_CONFIG_PLAYER_COUNT] as Long).toInt(),
        (map[GAME_CONFIG_ROUND_COUNT] as Long).toInt()
    )

private fun toPlayers(list: List<String>, mapper: ObjectMapper) : MutableList<Player> {
    val mutableList = mutableListOf<Player>()
    list.forEach {
        val playerDto = mapper.readValue(it, PlayerDto::class.java)
        mutableList.add(playerDto.toPlayer())
    }
    return mutableList
}

private fun DocumentSnapshot.toLobbyInfo(mapper: ObjectMapper) =
    LobbyInfo(
        id,
        data!![LOBBY_NAME] as String,
        toPlayers(data!![LOBBY_PLAYERS] as List<String>, mapper),
        toGameConfiguration(data!![LOBBY_GAME_CONFIG] as Map<String, Any>)
    )

private fun getUniquePlayerName(players: List<String>, playerName: String) : String {
    var tryCount = 1
    var name = playerName
    while(players.contains(name)) {
        ++tryCount
        name = "$playerName ($tryCount)"
    }
    return name
}

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
                onSuccess(result.map { it.toLobbyInfo(mapper) }.toList())
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    fun createLobby(lobbyName: String, playerName: String, gameConfiguration: GameConfiguration,
                    onSuccess: (LobbyInfo, Player) -> Unit,
                    onError: (Exception) -> Unit) {

        val player = Player(playerName, mutableListOf())
        val players = mutableListOf(player)
        val playersBlob = players.map { mapper.writeValueAsString(it.toDto()) }
        Firebase.firestore.collection(LOBBIES_COLLECTION)
            .add(hashMapOf(LOBBY_NAME to lobbyName, LOBBY_PLAYERS to playersBlob, LOBBY_GAME_CONFIG to gameConfiguration))
            .addOnSuccessListener {
                onSuccess(LobbyInfo(it.id, lobbyName, players, gameConfiguration), player) }
            .addOnFailureListener { onError(it) }
    }

    fun tryJoinLobby(lobbyId: String, playerName: String,
                     onSuccess: (LobbyInfo, Player) -> Unit,
                     onError: (Exception) -> Unit) {

        val document = Firebase.firestore
            .collection(LOBBIES_COLLECTION)
            .document(lobbyId)

        document
            .get()
            .addOnSuccessListener {
                val lobby = it.toLobbyInfo(mapper)
                val playerCount = lobby.gameConfig.playerCount

                if (lobby.players.size < playerCount) {
                    val uniquePlayerName = getUniquePlayerName(lobby.players.map { p -> p.name }, playerName)
                    val player = Player(uniquePlayerName, mutableListOf())

                    if (lobby.players.size == playerCount - 1) {
                        // Lobby is full
                        lobby.players.add(player)
                        document.delete()
                            .addOnSuccessListener {
                                Firebase.firestore
                                    .collection(GAMES_COLLECTION)
                                    .document(lobbyId)
                                    .set(hashMapOf(
                                        GAME_PLAYERS to lobby.players.map { player ->  mapper.writeValueAsString(player.toDto()) },
                                        GAME_DRAW_GUESSES to listOf()
                                    ))
                                    .addOnSuccessListener { onSuccess(lobby, player) }
                            }
                            .addOnFailureListener { onError(IllegalStateException("Lobby is already full")) }
                    } else {
                        // Lobby isn't full yet
                        document
                            .update(LOBBY_PLAYERS, FieldValue.arrayUnion(mapper.writeValueAsString(player.toDto())))
                            .addOnSuccessListener { onSuccess(lobby, player) }
                            .addOnFailureListener { err -> onError(err) }
                    }
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

    fun subscribeToLobby(lobbyId: String,
        onSubscriptionError: (Exception) -> Unit,
        onStateChange: (LobbyInfo) -> Unit) : ListenerRegistration {
        return Firebase.firestore
            .collection(LOBBIES_COLLECTION)
            .document(lobbyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onSubscriptionError(error)
                    return@addSnapshotListener
                }

                if (snapshot?.exists() == true) {
                    onStateChange(snapshot.toLobbyInfo(mapper))
                }
            }
    }

    fun exitLobby(lobbyId: String, player: Player) {
        Firebase.firestore
            .collection(LOBBIES_COLLECTION)
            .document(lobbyId)
            .update(LOBBY_PLAYERS, FieldValue.arrayRemove(mapper.writeValueAsString(player.toDto())))
    }
}