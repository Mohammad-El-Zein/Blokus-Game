package entity
import kotlinx.serialization.Serializable

/**
 * Represents a BlokusGameState.
 *
 * @constructor Creates a BlokusGameState
 *
 * @param players The players of the game
 * @param gameMode The mode of the game
 * @param advancedScoring Indicates whether advanced scoring should be used.
 * @param sharedColor The color shared by the players
 * @param colorOrder The order of the colors
 *
 * @property players The players of the game
 * @property gameMode The mode of the game
 * @property advancedScoring Indicates whether advanced scoring should be used.
 * @property sharedColor The color shared by the players
 * @property colorOrder The order of the colors
 */
@Serializable
data class BlokusGameState (
    val players: MutableList<Player>,
    val gameMode: GameMode,
    val advancedScoring: Boolean,
    val sharedColor: Color,
    val colorOrder: MutableList<Color>
) {
    /** Index of the shared color player */
    var sharedColorPlayerIndex: Int = 0
    /** game board */
    var gameBoard: MutableList<MutableList<Color>> = mutableListOf()
    /** Tracks which colors have passed - index matches colorOrder */
    var colorHasPassed: MutableList<Boolean> = mutableListOf()
}