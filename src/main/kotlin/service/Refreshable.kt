package service

/**
 * This interface provides a mechanism for the service layer classes to communicate
 * (usually to the GUI classes) that certain changes have been made to the entity
 * layer, so that the user interface can be updated accordingly.
 *
 * Default (empty) implementations are provided for all methods, so that implementing
 * GUI classes only need to react to events relevant to them.
 *
 * @see AbstractRefreshingService
 */
interface Refreshable {

    /** Called after a block has been rotated*/
    fun refreshAfterRotate() {}

    /** Called after a block has been mirrored*/
    fun refreshAfterMirror() {}

    /** Called after a block has been placed on the board*/
    fun refreshAfterBlockPlaced() {}

    /** Called after an undo action*/
    fun refreshAfterUndo() {}

    /** Called after a redo action*/
    fun refreshAfterRedo() {}

    /** Called after a game has been saved*/
    fun refreshAfterSaveGame() {}

    /** Called after a game has been loaded*/
    fun refreshAfterLoadGame() {}

    /** Called after a game has been created*/
    fun refreshAfterCreateGame() {}

    /** Called after a game has ended*/
    fun refreshAfterGameEnd() {}

    /** Called after a player has joined*/
    fun refreshAfterPlayerJoined() {}

    /** Called after a player turn has ended*/
    fun refreshAfterEndTurn() {}

    /**
     * refreshes the network connection status with the given information
     *
     * @param state the information to show
     */
    fun refreshConnectionState(state: ConnectionState) {}
}