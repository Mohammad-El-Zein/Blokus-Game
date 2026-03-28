package service.bot

import service.RootService
import kotlin.test.BeforeTest
import entity.BlockType
import entity.Color
import entity.GameMode
import entity.PlayerType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * this class tesst the fun calculatePossibleMove and make sure that the bot can correctly find all the legal
 * moves available in the board based on the current game stat
 */
class CalculatePossibleMovesTest {
    private lateinit var rootService: RootService
    private lateinit var botService: BotService

    /**
     * preparing the rootService and the BotService before each test run
     */
    @BeforeTest
    fun setup() {
        rootService = RootService()
        botService = rootService.botService
    }

    /**
     * helper fun that starts a standard 4p game, and it sets one bot and three huma players to creat a real game state
     */
    private fun create4PGame(){
        rootService.gameService.createGame(
            isAdvancedScoring = false,
            players = listOf(
                Triple("bot", Color.BLUE, PlayerType.STOCKFISHWANNABE),
                Triple("p2", Color.GREEN, PlayerType.PLAYER),
                Triple("p3", Color.YELLOW, PlayerType.PLAYER),
                Triple("p4", Color.RED, PlayerType.PLAYER),
            ),
            gameMode = GameMode.FOUR_PLAYER
        )
    }

    /**
     * tests that the bot returns an empty list of names if no game has started yet
     */
    @Test
    fun noGameEmptyList(){
        val deadline = System.currentTimeMillis() + BotConstants.MAX_THINKING_TIME_MS /** +++++++ */
        val moves = botService.calculatePossibleMoves(deadline)
        assertTrue(moves.isEmpty())
    }

    /**
     * test that the bot only finds moves that are actually legal and it clears the player´s blocks and just
     * one small block to verify that every move that the bot finds passes the "validatePlacement" check
     */
    @Test
    fun legalMovesCurrentPlayer(){
        val game = rootService.currentGame ?: return
        val currentPlayer = game.gameStates[game.currentStateIndex].players.first()
        val block = currentPlayer.playerBlocks.first() { it.blockType == BlockType.O1 }

        currentPlayer.playerBlocks.clear()
        currentPlayer.playerBlocks.add(block)

        val deadline = System.currentTimeMillis() + BotConstants.MAX_THINKING_TIME_MS /** +++++++ */
        val moves = botService.calculatePossibleMoves(deadline)

        assertTrue(moves.isEmpty())

        moves.forEach{ moves ->
            val choosenBlock = moves.first
            val x = moves.second.first
            val y = moves.second.second
            val rotation = moves.third.first
            val isMirrored = moves.third.second

            choosenBlock.rotation = rotation
            choosenBlock.isMirrored = isMirrored
            assertTrue(rootService.gameService.validatePlacement(x, y, choosenBlock))
        }
    }

    /**
     * this test tests if the current player has no blocks left in his hand, and the bot correctly returns
     * zero possible moves
     */
    @Test
    fun noBlocksNoMoves(){
        create4PGame()
        val game = rootService.currentGame ?: return
        val currentPlayer = game.gameStates[game.currentStateIndex].players.first()
        currentPlayer.playerBlocks.clear()
        val deadline = System.currentTimeMillis() + BotConstants.MAX_THINKING_TIME_MS /** +++++++ */
        val moves = botService.calculatePossibleMoves(deadline)

        assertEquals(0, moves.size)
    }
}