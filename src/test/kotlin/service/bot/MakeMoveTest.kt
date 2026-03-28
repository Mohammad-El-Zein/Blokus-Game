package service.bot

import entity.Block
import entity.BlockType
import entity.Color
import entity.GameMode
import entity.PlayerType
import entity.Rotation
import org.junit.jupiter.api.BeforeEach
import service.RootService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * this class checks if the bot correctly places blocks on the board, handels invalid moves and reacts properly
 * when no game is active
 *
 * @property rootService the main service that connects all game logic
 * @property botService the service that manages bot decisions and action
 */
class MakeMoveTest {
    private lateinit var rootService: RootService
    private lateinit var botService: BotService


    /**
     * sets up the RootService and BotService before each test runs
     */
    @BeforeEach
    fun setup() {
        rootService = RootService()
        botService = rootService.botService
    }

    /**
     * a helper fun to start a new 4p game with a specific type for the first player
     *
     * @param firstPlayerType defines if the 1st plaer is human or bot
     */
    private fun create4PGame(firstPlayerType: PlayerType = PlayerType.PLAYER){
        rootService.gameService.createGame(
            isAdvancedScoring = false,
            players = listOf(
                Triple("p1", Color.BLUE, firstPlayerType),
                Triple("p2", Color.GREEN, firstPlayerType),
                Triple("p3", Color.YELLOW, firstPlayerType),
                Triple("p4", Color.RED, firstPlayerType),
            ),
            gameMode = GameMode.FOUR_PLAYER
        )
    }

    /**
     * test if the fun stop immediatly if there is no game started and ensure the rootService remains empty
     * and no errors occur
     */
    @Test
    fun noActiveGame(){
        val move = Triple(
            Block(Color.BLUE, BlockType.O1),
            Pair(0 ,0),
            Pair(Rotation.NONE, false)
        )
        botService.makeMove(move)

        assertEquals(null, rootService.currentGame)
    }

    /**
     * test if the vailed move correctly placed a block on the game board and it verifies that the piece is removed
     * from the player´s hand and the board is updated
     */
    @Test
    fun placeValidBlock(){
        create4PGame()
        val game = rootService.currentGame ?: return
        val currentPlayer = game.gameStates[game.currentStateIndex].players.first()
        val block = currentPlayer.playerBlocks.first { it.blockType == BlockType.O1 }
        val move = Triple(block, Pair(0, 0), Pair(Rotation.NONE, false))

        botService.makeMove(move)

        assertEquals(1, game.currentStateIndex)

        val newState = game.gameStates[game.currentStateIndex]
        val newPlayer = newState.players.last()

        assertFalse(newPlayer.playerBlocks.any { it.blockType == BlockType.O1 })
        assertEquals(1, newState.gameBoard.flatten().count(){it == Color.BLUE})
        assertEquals(Rotation.NONE, block.rotation)
        assertFalse(block.isMirrored)
    }

    /**
     * test what happens when the bot tries to make an illegal move and check if the bot correctly skiped its turn
     * and marks the color as "passed"
     */
    @Test
    fun invalidThenPassAndEndTurn(){
        create4PGame()
        val game = rootService.currentGame ?: return
        val state = game.gameStates[game.currentStateIndex]
        val currentPlayer = state.players.first()
        val block = currentPlayer.playerBlocks.first { it.blockType == BlockType.O1 }
        val move = Triple(block, Pair(1, 1), Pair(Rotation.NONE, false))

        botService.makeMove(move)

        assertEquals(0, game.currentStateIndex)
        assertEquals("p2", state.players.first().name)
        assertTrue(state.players.last().playerBlocks.any { it.blockType == BlockType.O1 })
        assertEquals(0, state.gameBoard.flatten().count{it == Color.RED})
    }
}
