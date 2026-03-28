package service

import edu.udo.cs.sopra.ntf.ActionMessage
import edu.udo.cs.sopra.ntf.InitMessage
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.CreateGameResponseStatus
import tools.aqua.bgw.net.common.response.GameActionResponse
import tools.aqua.bgw.net.common.response.GameActionResponseStatus
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus

/**
 * BoardGameClient implementation for network communication
 *
 * @param playerName the name of the player using this client
 * @param host the host to connect to
 * @param secret the secret to use for the connection
 * @property networkService the NetworkService to potentially forward received messages to
 * */
class BlokusNetworkClient(
    playerName: String,
    host: String,
    secret: String,
    var networkService: NetworkService,
): BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {
    /** the identifier of this game session; can be null if no session started yet. */
    var sessionID: String? = null

    /**
     * Handle a CreateGameResponse sent by the server. Will await the guest player when its
     * status is CreateGameResponseStatus.SUCCESS. As recovery from network problems is not
     * implemented in Blokus, the method disconnects from the server and throws an
     * IllegalStateException otherwise.
     *
     * @throws IllegalStateException if status != success or currently not waiting for a game creation response.
     */
    override fun onCreateGameResponse(response: CreateGameResponse){
        check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION){
            "unexpected CreateGameResponse"
        }

        when (response.status) {
            CreateGameResponseStatus.SUCCESS -> {
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_GUESTS)
                sessionID = response.sessionID
            }
            else -> disconnectAndError(response.status)
        }
    }

    /**
     * Handle a JoinGameResponse sent by the server. Will await the init message when its
     * status is JoinGameResponseStatus.SUCCESS. As recovery from network problems is not
     * implemented in Blokus, the method disconnects from the server and throws an
     * IllegalStateException otherwise.
     *
     * @throws IllegalStateException if status != success or currently not waiting for a join game response.
     */
    override fun onJoinGameResponse(response: JoinGameResponse){
        check(networkService.connectionState == ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
        { "unexpected JoinGameResponse" }

        when (response.status) {
            JoinGameResponseStatus.SUCCESS -> {
                sessionID = response.sessionID
                networkService.updateConnectionState(ConnectionState.WAITING_FOR_INIT)
            }
            else -> disconnectAndError(response.status)
        }
    }

    /**
     * Handle a PlayerJoinedNotification sent by the server. As Blokus supports 2-4 Players, this will
     * stay in the current state, until the game is manually started.
     * also ensures that players are identifiable by their names
     *
     * @throws IllegalStateException if not currently expecting any guests to join.
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification){
        //check(networkService.connectionState == ConnectionState.WAITING_FOR_GUESTS)
        //{ "not awaiting any guests" }

        /* add sender name to players, leave if newPlayer already exists */
        val newPlayer = notification.sender
        if (newPlayer.isNotBlank() && !networkService.networkPlayers.contains(newPlayer))
        { networkService.networkPlayers.add(newPlayer)
            networkService.onAllRefreshables { refreshAfterPlayerJoined() }
        }

        else { disconnectAndError("illegal player name") }
    }

    /**
     * Handle a GameActionResponse sent by the server. Does nothing when its
     * status is GameActionResponseStatus.SUCCESS. As recovery from network problems is not
     * implemented in Blokus, the method disconnects from the server and throws an
     * IllegalStateException otherwise.
     */
    override fun onGameActionResponse(response: GameActionResponse) {
        println("GameActionReceived")
        check(networkService.connectionState == ConnectionState.PLAYING_MY_TURN ||
                networkService.connectionState == ConnectionState.WAIT_FOR_MY_TURN)
        { "not currently playing in a network game" }

        when (response.status) {
            GameActionResponseStatus.SUCCESS -> {}      // do nothing
            else -> disconnectAndError(response.status)
        }
    }

    /**
     * handle an InitMessage sent by the server. creates a game based on the details specified
     * in the message
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onInitReceived(message: InitMessage, sender: String){
        println("onInitReceivedClient")
        check(networkService.connectionState == ConnectionState.WAITING_FOR_INIT)
        { "unexpected init" }

        networkService.receiveInit(message)
    }

    /**
     * Handle an ActionMessage sent by the server. Does nothing when its
     * status is GameActionResponseStatus.SUCCESS. As recovery from network problems is not
     * implemented in Blokus, the method disconnects from the server and throws an
     * IllegalStateException otherwise.
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onActionReceived(message: ActionMessage, sender: String){
        networkService.receiveAction(message, sender)
    }

    private fun disconnectAndError(message: Any) {
        networkService.disconnect()
        networkService.networkPlayers.clear()
        error(message)
    }
}