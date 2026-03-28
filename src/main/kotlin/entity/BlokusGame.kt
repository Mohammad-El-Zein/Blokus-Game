package entity
import kotlinx.serialization.Serializable

/**
 * Represents the BlokusGame.
 *
 * @param blokusGameState The current instance of the Blokus game.
 *
 * @property blokusGameState The current instance of the Blokus game.
 */
@Serializable
class BlokusGame (val blokusGameState: BlokusGameState) {
    /** The last move done */
    var currentStateIndex : Int = 0
    var gameStates : MutableList<BlokusGameState> = mutableListOf(blokusGameState)
}