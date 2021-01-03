package edu.isel.pdm.li51xd.g08.drag.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.RequestQueue
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import edu.isel.pdm.li51xd.g08.drag.BuildConfig
import edu.isel.pdm.li51xd.g08.drag.game.model.DrawGuess
import edu.isel.pdm.li51xd.g08.drag.game.model.GameConfiguration
import edu.isel.pdm.li51xd.g08.drag.game.remote.*

private const val LOBBIES_COLLECTION = "lobbies"
private const val GAMES_COLLECTION = "games"
private const val DRAW_GUESS_COLLECTION = "drawGuesses"

private const val LOBBY_NAME = "name"
private const val LOBBY_PLAYERS = "players"
private const val LOBBY_GAME_CONFIG = "gameConfig"
private const val GAME_CONFIG_PLAYER_COUNT = "playerCount"
private const val GAME_CONFIG_ROUND_COUNT = "roundCount"

private const val GAME_PLAYERS = "players"

private const val DG_DRAW_GUESS = "drawGuess"
private const val DG_BOOK_OWNER = "bookOwnerId"

private fun toGameConfiguration(map: Map<String, Any>) =
    GameConfiguration(
        (map[GAME_CONFIG_PLAYER_COUNT] as Long).toInt(),
        (map[GAME_CONFIG_ROUND_COUNT] as Long).toInt()
    )

private fun toPlayers(list: List<String>, mapper: ObjectMapper) : MutableList<Player> {
    val mutableList = mutableListOf<Player>()
    list.forEach {
        val playerDto = mapper.readValue(it, PlayerDto::class.java)
        mutableList.add(playerDto.toPlayer(mapper))
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

private fun DocumentSnapshot.toGameInfo(mapper: ObjectMapper) =
    GameInfo(
        id,
        toPlayers(data!![LOBBY_PLAYERS] as List<String>, mapper)
    )

private fun DocumentSnapshot.toPlayerDrawGuess(mapper: ObjectMapper) =
        PlayerDrawGuessDto(
                data!![DG_BOOK_OWNER] as String,
                mapper.readValue(data!![DG_DRAW_GUESS] as String, DrawGuessDto::class.java)
        ).toPlayerDrawGuess(mapper)

class DragRepository(private val queue: RequestQueue, private val mapper: ObjectMapper) {

    // Disable offline cache of data
    init {
        val settings = firestoreSettings {
            isPersistenceEnabled = false
        }
        Firebase.firestore.firestoreSettings = settings
    }

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

        val player = Player(playerName)
        val players = mutableListOf(player)
        val playersBlob = players.map { mapper.writeValueAsString(it.toDto(mapper)) }
        Firebase.firestore.collection(LOBBIES_COLLECTION)
            .add(hashMapOf(LOBBY_NAME to lobbyName, LOBBY_PLAYERS to playersBlob, LOBBY_GAME_CONFIG to gameConfiguration))
            .addOnSuccessListener {
                onSuccess(LobbyInfo(it.id, lobbyName, players, gameConfiguration), player) }
            .addOnFailureListener { onError(it) }
    }

    fun createGame(gameId: String, players: List<Player>,
                   onSuccess: () -> Unit) {
        Firebase.firestore.collection(GAMES_COLLECTION)
            .document(gameId)
            .set(hashMapOf(
                GAME_PLAYERS to players.map { player ->  mapper.writeValueAsString(player.toDto(mapper)) }
            ))
            .addOnSuccessListener { onSuccess() }
    }

    fun tryJoinLobby(lobbyId: String, playerName: String,
                     onSuccess: (LobbyInfo, Player) -> Unit,
                     onError: (Exception) -> Unit) {

        val document = Firebase.firestore.collection(LOBBIES_COLLECTION)
            .document(lobbyId)

        document.get().addOnSuccessListener {
                if (!it.exists()) {
                    onError(IllegalArgumentException())
                    return@addOnSuccessListener
                }

                val lobby = it.toLobbyInfo(mapper)
                val playerCount = lobby.gameConfig.playerCount
                if (lobby.players.size < playerCount) {
                    val player = Player(playerName)

                    if (lobby.players.size == playerCount - 1) {
                        // Lobby is full
                        lobby.players.add(player)
                        document.delete()
                            .addOnSuccessListener {
                                createGame(lobbyId, lobby.players) {
                                    onSuccess(lobby, player)
                                }
                            }
                            .addOnFailureListener { onError(IllegalStateException("Lobby is already full")) }
                    } else {
                        // Lobby isn't full yet
                        document
                            .update(LOBBY_PLAYERS, FieldValue.arrayUnion(mapper.writeValueAsString(player.toDto(mapper))))
                            .addOnSuccessListener { onSuccess(lobby, player) }
                            .addOnFailureListener { err -> onError(err) }
                    }
                }
            }
            .addOnFailureListener { onError(it) }
    }

    fun exitLobby(lobbyId: String, player: Player) {
        val document = Firebase.firestore
                .collection(LOBBIES_COLLECTION)
                .document(lobbyId)
        document
                .get()
                .addOnSuccessListener {
                    val lobby = it.toLobbyInfo(mapper)
                    if (lobby.players.size == 1) {
                        document.delete()
                    } else {
                        document
                                .update(LOBBY_PLAYERS, FieldValue.arrayRemove(mapper.writeValueAsString(player.toDto(mapper))))
                    }
                }
    }

    fun exitGame(gameId: String, player: Player) {
        val document = Firebase.firestore
            .collection(GAMES_COLLECTION)
            .document(gameId)
        document
            .get()
            .addOnSuccessListener {
                val game = it.toGameInfo(mapper)
                if (game.players.size == 1) {
                    document.delete()
                } else {
                    document
                        .update(GAME_PLAYERS, FieldValue.arrayRemove(mapper.writeValueAsString(player.toDto(mapper))))
                }
            }
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

    fun subscribeToGame(gameId: String,
                         onSubscriptionError: (Exception) -> Unit,
                         onStateChange: (GameInfo) -> Unit) : ListenerRegistration {
        return Firebase.firestore
                .collection(GAMES_COLLECTION)
                .document(gameId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onSubscriptionError(error)
                        return@addSnapshotListener
                    }
                    if (snapshot?.exists() == true) {
                        onStateChange(snapshot.toGameInfo(mapper))
                    }
                }
    }

    fun subscribeToDrawGuess(gameId: String, playerId: String,
                             onSubscriptionError: (Exception) -> Unit,
                             onStateChange: (PlayerDrawGuess) -> Unit) : ListenerRegistration {
        val doc = Firebase.firestore
                .collection(DRAW_GUESS_COLLECTION)
                .document("${gameId}-${playerId}")
        return doc
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        onSubscriptionError(error)
                        return@addSnapshotListener
                    }
                    if (snapshot?.exists() == true) {
                        val playerDrawGuess = snapshot.toPlayerDrawGuess(mapper)
                        doc.delete()
                            .addOnSuccessListener { onStateChange(playerDrawGuess) }
                            .addOnFailureListener { onSubscriptionError(IllegalArgumentException()) }
                    }
                }
    }

    fun sendDrawGuess(gameId: String, receiverId: String, bookOwnerId: String, drawGuess: DrawGuess) {
        val dto = PlayerDrawGuess(bookOwnerId, drawGuess).toDto(mapper)
        Firebase.firestore
            .collection(DRAW_GUESS_COLLECTION)
            .document("${gameId}-${receiverId}")
            .set(hashMapOf(DG_BOOK_OWNER to dto.bookOwnerId, DG_DRAW_GUESS to mapper.writeValueAsString(dto.drawGuess)))
    }

    fun addDrawGuessToBook(gameId: String, bookOwnerId: String, drawGuess: DrawGuess) {
        val doc = Firebase.firestore
            .collection(GAMES_COLLECTION)
            .document(gameId)
        doc.get().addOnSuccessListener {
                val gameInfo = it.toGameInfo(mapper)
                for(i in 0 until gameInfo.players.size) {
                    val player = gameInfo.players[i]
                    if (player.id == bookOwnerId) {
                        doc.update(GAME_PLAYERS, FieldValue.arrayRemove(mapper.writeValueAsString(player.toDto(mapper))))
                        player.book.add(drawGuess)
                        doc.update(GAME_PLAYERS, FieldValue.arrayUnion(mapper.writeValueAsString(player.toDto(mapper))))
                        break
                    }
                }
            }
    }
}