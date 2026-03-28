package entity
import kotlinx.serialization.Serializable

/**
 * Represents a player in a Blokus game.
 *
 * @param name The name of the player.
 * @param playerType The type of the player.
 *
 * @property name The name of the player.
 * @property playerType The type of the player.
 */
@Serializable
class Player (val name: String, val playerType: PlayerType){
    /** The score of the player */
    var score: Int = 0

    /** The blocks of the player */
    var playerBlocks: MutableList<Block> = mutableListOf()
}