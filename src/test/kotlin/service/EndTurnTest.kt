package service

import entity.*
import kotlin.test.*

/** Tests for the endTurn() method in GameService.*/

class EndTurnTest {

    private lateinit var rootService: RootService

    /**
     * Sets up the test environment before each test.
     * Creates a RootService with a FOUR_PLAYER gameMode and four players A , B, C, D.
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
        val players = mutableListOf(
            Player("Player_A", PlayerType.PLAYER),
            Player("Player_B", PlayerType.PLAYER),
            Player("Player_C", PlayerType.PLAYER),
            Player("Player_D", PlayerType.PLAYER)
        )
        val state = BlokusGameState(
            players = players,
            gameMode = GameMode.FOUR_PLAYER,
            advancedScoring = false,
            sharedColor = Color.BLANK,
            colorOrder = mutableListOf(Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN)
        )

        state.colorHasPassed = MutableList(4) { false } // alle color nicht gepasst, alle spieler macht turn
        state.gameBoard = MutableList(22) { MutableList(22) { Color.BLANK } }
        for (i in 0 until 22) {
            state.gameBoard[i][0] = Color.BLOCKED
            state.gameBoard[i][21] = Color.BLOCKED
            state.gameBoard[0][i] = Color.BLOCKED
            state.gameBoard[21][i] = Color.BLOCKED
        }
        state.gameBoard[0][0] = Color.CORNER
        state.gameBoard[0][21] = Color.CORNER
        state.gameBoard[21][0] = Color.CORNER
        state.gameBoard[21][21] = Color.CORNER

        players[0].playerBlocks.add(Block(Color.RED, BlockType.O1))
        players[1].playerBlocks.add(Block(Color.YELLOW, BlockType.O1))
        players[2].playerBlocks.add(Block(Color.BLUE, BlockType.O1))
        players[3].playerBlocks.add(Block(Color.GREEN, BlockType.O1))
        val game = BlokusGame(state)
        rootService.currentGame = game
    }

    /**
     * Tests that the next player is correctly determined after endTurn().
     */
    @Test
    fun `next player is correct after endTurn`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        assertEquals("Player_A", state.players.first().name)

        rootService.gameService.endTurn()
        assertEquals("Player_B", rootService.gameService.getCurrentPlayer().name)
    }

    /**
     * Tests that endTurn() throws an exception when no game is active.
     */
    @Test
    fun `endTurn throws exception when no active game game`() {
        rootService.currentGame = null
        assertFailsWith<IllegalStateException> {
            rootService.gameService.endTurn()
        }
    }

    /**
     * Tests that colorOrder is rotated correctly after endTurn().
     */
    @Test
    fun `colorOrder is rotated correctly after endTurn`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        assertEquals(Color.RED, state.colorOrder.first())

        rootService.gameService.endTurn()


        assertEquals(Color.YELLOW, state.colorOrder.first())
        assertEquals(Color.RED, state.colorOrder.last())


    }

    /**
     * Tests that colorHasPassed is set correctly when no move is possible.
     */
    @Test
    fun `colorHasPassed is set when no move possible`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        state.players.first().playerBlocks.clear()

        rootService.gameService.endTurn()


        assertEquals(true, state.colorHasPassed.last())
    }

}

