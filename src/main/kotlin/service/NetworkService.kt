package service

import edu.udo.cs.sopra.ntf.ActionMessage
import edu.udo.cs.sopra.ntf.InitMessage
import entity.Block
import entity.PlayerType
import edu.udo.cs.sopra.ntf.Color as NTFColor           // standard set by the NTF
import edu.udo.cs.sopra.ntf.GameMode as NTFGameMode
import edu.udo.cs.sopra.ntf.Rotation as NTFRotation
import edu.udo.cs.sopra.ntf.BlockType as NTFBlockType
import entity.Color as G7Color                          // we are Group 7
import entity.GameMode as G7GameMode
import entity.Rotation as G7Rotation
import entity.BlockType as G7BlockType

/** a class that handles the logic for sending and receiving messages; bridges the gap between BlokusNetworkClient
 * and the other classes
 *
 * @param rootService the RootService instance to access the other services and the entity layer
 * */
class NetworkService(private val rootService: RootService): AbstractRefreshingService() {
    /** Network client. Nullable for offline games. */
    var client: BlokusNetworkClient? = null
        private set

    /** current state of the connection in a network game. */
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        private set

    /** list of playerNames that joined the hosted Lobby (including the host's name); names must be unique */
    val networkPlayers: MutableList<String> = mutableListOf()

    /** determines what type of player is playing client side, default: human */
    var clientPlayerType = PlayerType.PLAYER

    /**
     * Connects to server, sets the NetworkService.client if successful and returns `true` on success.
     *
     * @param name Player name. Must not be blank
     *
     * @throws IllegalArgumentException if secret or name is blank
     * @throws IllegalStateException if already connected to another game
     */
    private fun connect(name: String): Boolean{
        require(connectionState == ConnectionState.DISCONNECTED && client == null)
        { "already connected to another game" }

        require(name.isNotBlank()) { "player name must be given" }

        val newClient =
            BlokusNetworkClient(
                playerName = name,
                host = SERVER_ADDRESS,
                secret = SERVER_SECRET,
                networkService = this
            )

        return if (newClient.connect()) {
            this.client = newClient
            true
        } else {
            false
        }
    }

    /**
     * Connects to server and creates a new game session.
     *
     * @param name Player name.
     * @param sessionID identifier of the hosted session (to be used by guest on join)
     *
     * @throws IllegalStateException if already connected to another game or connection attempt fails
     */
    fun hostGame(name: String, sessionID: String, playerType: PlayerType){
        if (!connect(name)) { error("connection failed") }
        updateConnectionState(ConnectionState.CONNECTED)

        client?.createGame(gameID = GAME_ID, sessionID = sessionID, greetingMessage = "")
        networkPlayers.add(name)
        clientPlayerType = playerType

        updateConnectionState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
    }

    /**
     * Connects to the server and joins
     *
     * @param name Player name
     * @param sessionID identifier of the hosted session (that you want to join)
     *
     * @throws IllegalStateException if a connection cannot get established
     * */
    fun joinGame(name: String, sessionID: String, playerType: PlayerType){
        if (!connect(name)) { error("connection failed") }
        updateConnectionState(ConnectionState.CONNECTED)

        client?.joinGame(sessionID = sessionID, greetingMessage = "")
        clientPlayerType = playerType

        updateConnectionState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
    }

    /**
     * sends a message to the players that specifies the game settings the host made
     *
     * @param isAdvancedScoring indicates if advanced scoring is used
     * @param players list of all players in the game and the colors they are assigned
     * @param gamemode the gamemode of the game
     *
     * @throws IllegalStateException if the game is not ready to start a match
     * */
    fun sendInit(isAdvancedScoring: Boolean, players: List<Triple<String, G7Color, PlayerType>>, gamemode: G7GameMode){
        check(connectionState == ConnectionState.WAITING_FOR_GUESTS)
        { "currently not prepared to start a new hosted game" }

        val ntfPlayers = mutableListOf<Pair<String, NTFColor>>()
        for (player in players) {
            val ntfPair = Pair(player.first, player.second.toNTFColor())
            ntfPlayers.add(ntfPair)
        }
        ntfPlayers.toList()

        val nftGameMode = gamemode.toNTFGameMode()

        val message = InitMessage(
            isAdvancedScoring = isAdvancedScoring,
            players = ntfPlayers,
            gameMode = nftGameMode
        )

        if (client?.playerName == players[0].first) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        } else {
            updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)
        }

        client?.sendGameActionMessage(message)
    }

    /** converts our color to the NTF-Standard */
    fun G7Color.toNTFColor(): NTFColor {
        return when(this){
            G7Color.BLUE -> NTFColor.BLUE
            G7Color.YELLOW -> NTFColor.YELLOW
            G7Color.RED -> NTFColor.RED
            G7Color.GREEN -> NTFColor.GREEN
            else -> error("could not convert to NTFColor")
        }
    }

    /** converts our gameMode to the NTF-Standard */
    fun G7GameMode.toNTFGameMode(): NTFGameMode {
        return when(this){
            G7GameMode.FOUR_PLAYER -> NTFGameMode.FOUR_PLAYER
            G7GameMode.THREE_PLAYER -> NTFGameMode.THREE_PLAYER
            G7GameMode.TWO_PLAYER_FOUR_COLOR -> NTFGameMode.TWO_PLAYER
            G7GameMode.TWO_PLAYER_SMALL -> NTFGameMode.TWO_PLAYER_SMALL
        }
    }

    /**
     * creates a game based on the init message received
     *
     * @param message the InitMessage sent by the host
     *
     * @throws IllegalStateException if an init is not expected at the time
     * */
    fun receiveInit(message: InitMessage){
        println("test1")
        val localPlayers = mutableListOf<Triple<String, G7Color, PlayerType>>()
        for (player in message.players) {

            val localTriple = if (player.first == client?.playerName){
                Triple(player.first, player.second.toG7Color(), clientPlayerType)
            } else {
                Triple(player.first, player.second.toG7Color(), PlayerType.PLAYER)
            }
            println("test2 $localTriple")
            localPlayers.add(localTriple)
        }
        val localPlayersList = localPlayers.toList()

        val localGameMode = message.gameMode.toG7GameMode()
        println("test3")
        if (client?.playerName == localPlayers[0].first){
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        } else {
            updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)
        }
        println("test4")
        rootService.gameService.createGame(message.isAdvancedScoring, localPlayersList, localGameMode)
    }

    /** converts the NTFColor to our Standard */
    fun NTFColor.toG7Color(): G7Color {
        return when(this){
            NTFColor.BLUE -> G7Color.BLUE
            NTFColor.YELLOW -> G7Color.YELLOW
            NTFColor.RED -> G7Color.RED
            NTFColor.GREEN -> G7Color.GREEN
        }
    }

    /** converts the NTFGameMode to our Standard */
    fun NTFGameMode.toG7GameMode(): G7GameMode {
        return when(this){
            NTFGameMode.FOUR_PLAYER -> G7GameMode.FOUR_PLAYER
            NTFGameMode.THREE_PLAYER -> G7GameMode.THREE_PLAYER
            NTFGameMode.TWO_PLAYER -> G7GameMode.TWO_PLAYER_FOUR_COLOR
            NTFGameMode.TWO_PLAYER_SMALL -> G7GameMode.TWO_PLAYER_SMALL
        }
    }

    /**
     * sends a message informing all players of the move that the player just did
     *
     * @param isMirrored weather or not the block is mirrored
     * @param coords the coordinates(x, y) that the block is getting placed at (top left of the block's location)
     * @param rotation how far the block is rotated
     * @param blockType the unique type of the block that gets placed
     *
     * @throws IllegalStateException when it is not currently the player's turn
     * */
    fun sendAction(isMirrored: Boolean, coords: Pair<Int, Int>, rotation: G7Rotation, blockType: G7BlockType) {
        if ( rootService.gameService.getCurrentPlayer().name == client?.playerName ){
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }

        check(connectionState == ConnectionState.PLAYING_MY_TURN)
        { "not your turn"}

        val ntfRotation = rotation.toNTFRotation()
        val ntfBlockType = blockType.toNTFBlockType()

        val message = ActionMessage(
            isMirrored = isMirrored,
            coords = coords,
            rotation = ntfRotation,
            blockType = ntfBlockType
        )

        /* if (rootService.gameService.getCurrentPlayer().name != client?.playerName)
        { updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN) } */
        //if (fetchNextPlayer().name != client?.playerName)
        //{ updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN) }
        updateConnectionState(ConnectionState.WAIT_FOR_MY_TURN)

        client?.sendGameActionMessage(message)
    }

    /** converts our blockrotation to the NTF-Standard */
    fun G7Rotation.toNTFRotation(): NTFRotation {
        return when(this){
            G7Rotation.NONE -> NTFRotation.NONE
            G7Rotation.RIGHT -> NTFRotation.NINETY
            G7Rotation.BOTTOM -> NTFRotation.ONEHUNDREDANDEIGHTY
            G7Rotation.LEFT -> NTFRotation.TWOHUNDREDANDSEVENTY
        }
    }

    /** converts our blockType to the NTF-Standard */
    fun G7BlockType.toNTFBlockType(): NTFBlockType {
        return when(this){
            G7BlockType.I5 -> NTFBlockType.I5
            G7BlockType.N5 -> NTFBlockType.N5
            G7BlockType.V5 -> NTFBlockType.V5
            G7BlockType.T5 -> NTFBlockType.T5
            G7BlockType.U5 -> NTFBlockType.U5
            G7BlockType.L5 -> NTFBlockType.L5
            G7BlockType.Y5 -> NTFBlockType.Y5
            G7BlockType.Z5 -> NTFBlockType.Z5
            G7BlockType.W5 -> NTFBlockType.W5
            G7BlockType.P5 -> NTFBlockType.P5
            G7BlockType.X5 -> NTFBlockType.X5
            G7BlockType.F5 -> NTFBlockType.F5
            G7BlockType.Z4 -> NTFBlockType.Z4
            G7BlockType.I4 -> NTFBlockType.I4
            G7BlockType.L4 -> NTFBlockType.L4
            G7BlockType.O4 -> NTFBlockType.O4
            G7BlockType.T4 -> NTFBlockType.T4
            G7BlockType.I3 -> NTFBlockType.I3
            G7BlockType.V3 -> NTFBlockType.V3
            G7BlockType.I2 -> NTFBlockType.I2
            G7BlockType.O1 -> NTFBlockType.O1
        }
    }

    /* private fun fetchNextPlayer(): Player {
        val game = checkNotNull(rootService.currentGame){"no current game"}
        val currentGameState = game.currentStateIndex
        val currentGame = game.gameStates[currentGameState]

        if (currentGame.gameMode == G7GameMode.THREE_PLAYER && currentGame.colorOrder[1] == currentGame.sharedColor){
            val idx = (currentGame.sharedColorPlayerIndex + 1) % 3 // adjust idx to current rotation of players
            return currentGame.players[idx]
        }
        return currentGame.players[1]
    } */

    /**
     * plays an opponent's turn based on the ActionMessage received
     *
     * @param message the ActionMessage sent by a player
     * @param sender the name of the player who sent the ActionMessage
     *
     * @throws IllegalStateException if the system doesn't expect to receive an action at the time
     * */
    fun receiveAction(message: ActionMessage, sender: String){
        check(connectionState == ConnectionState.WAIT_FOR_MY_TURN &&
                rootService.gameService.getCurrentPlayer().name == sender)
        { "illegal player action" }

        val localBlockType = message.blockType.toG7BlockType()
        val nrRotations = message.rotation.ordinal

        val placedBlock = fetchPlacedBlock(localBlockType)
        if (message.isMirrored){ rootService.playerActionService.mirror(placedBlock) }
        repeat(nrRotations) {rootService.playerActionService.rotate(placedBlock)}

        rootService.playerActionService.placeBlock(
            placedBlock, message.coords.first, message.coords.second)

        if (rootService.gameService.getCurrentPlayer().name == client?.playerName) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }
        /*if (fetchNextPlayer().name == client?.playerName) {
            updateConnectionState(ConnectionState.PLAYING_MY_TURN)
        }*/
    }

    /** converts the NTFBlockType to our standard */
    fun NTFBlockType.toG7BlockType(): G7BlockType {
        return when(this){
            NTFBlockType.I5 -> G7BlockType.I5
            NTFBlockType.N5 -> G7BlockType.N5
            NTFBlockType.V5 -> G7BlockType.V5
            NTFBlockType.T5 -> G7BlockType.T5
            NTFBlockType.U5 -> G7BlockType.U5
            NTFBlockType.L5 -> G7BlockType.L5
            NTFBlockType.Y5 -> G7BlockType.Y5
            NTFBlockType.Z5 -> G7BlockType.Z5
            NTFBlockType.W5 -> G7BlockType.W5
            NTFBlockType.P5 -> G7BlockType.P5
            NTFBlockType.X5 -> G7BlockType.X5
            NTFBlockType.F5 -> G7BlockType.F5
            NTFBlockType.Z4 -> G7BlockType.Z4
            NTFBlockType.I4 -> G7BlockType.I4
            NTFBlockType.L4 -> G7BlockType.L4
            NTFBlockType.O4 -> G7BlockType.O4
            NTFBlockType.T4 -> G7BlockType.T4
            NTFBlockType.I3 -> G7BlockType.I3
            NTFBlockType.V3 -> G7BlockType.V3
            NTFBlockType.I2 -> G7BlockType.I2
            NTFBlockType.O1 -> G7BlockType.O1
        }
    }

    private fun fetchPlacedBlock(type: G7BlockType): Block {
        val game = checkNotNull(rootService.currentGame){"no current game"}
        val currentGameState = game.currentStateIndex
        val blockColor = game.gameStates[currentGameState].colorOrder[0]   // having a log would have made this easier
        val currentPlayer = rootService.gameService.getCurrentPlayer()

        for (block in currentPlayer.playerBlocks){
            if ( block.blockType == type && block.color == blockColor ){
                return block
            }
        }
        error("no block found")
    }

    /**
     * Disconnects the client from the server, nulls it and updates the
     * connectionState to ConnectionState.DISCONNECTED. Can safely be called
     * even if no connection is currently active.
     */
    fun disconnect(){
        client?.apply {
            if (sessionID != null) leaveGame("Goodbye!")
            if (isOpen) disconnect()
        }
        client = null
        networkPlayers.clear()
        updateConnectionState(ConnectionState.DISCONNECTED)
    }

    /**
     * Updates the connectionState to newState and notifies
     * all refreshables via Refreshable.refreshConnectionState
     */
    fun updateConnectionState(newState: ConnectionState) {
        this.connectionState = newState
        onAllRefreshables {
            refreshConnectionState(newState)
        }
    }

    /** a companion object, that holds the server-address, secret and gameID */
    companion object {
        /** URL of the BGW net server hosted for SoPra participants */
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"

        /** the server secret for this SoPra */
        const val SERVER_SECRET = "blocksAgain"

        /** Name of the game as registered with the server */
        const val GAME_ID = "Blokus"
    }
}