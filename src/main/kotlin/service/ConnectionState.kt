package service

/** an enum class to distinguish between connection states */
enum class ConnectionState {
    /** no connection active; the initial state */
    DISCONNECTED,
    /** connected to server, but no game started or joined yet */
    CONNECTED,
    /** hostGame request has been sent to the server. waiting for confirmation (i.e. CreateGameResponse) */
    WAITING_FOR_HOST_CONFIRMATION,
    /** joinGame request has been sent to the server. waiting for confirmation (i.e. JoinGameResponse) */
    WAITING_FOR_JOIN_CONFIRMATION,
    /** host game started. waiting for guests to join */
    WAITING_FOR_GUESTS,
    /** joined game as a guest. waiting for host to send init message (i.e. InitMessage) */
    WAITING_FOR_INIT,
    /** game is running. it is my turn. */
    PLAYING_MY_TURN,
    /** game is running. it is not my turn. waiting for opponent(s) to send their turn(s) */
    WAIT_FOR_MY_TURN
}