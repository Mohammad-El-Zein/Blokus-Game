package service

import entity.*
import kotlin.test.*

/** Tests for the checkForPossibleMove() method in GameService.*/
class CheckForPossibleMoveTest {

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
            colorOrder = mutableListOf(Color.BLUE, Color.YELLOW, Color.RED, Color.GREEN)
        )
        state.colorHasPassed = MutableList(4) { false }

        //initiale board game, 20x20 blank, 2x2 corner und blocked
        state.gameBoard = MutableList(22) { MutableList(22) { Color.BLANK } }

        //von createGameBoard übernehmen
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

        val game = BlokusGame(state)
        rootService.currentGame = game
    }

    /**
     * Tests that colorHasPassed is set when player has no blocks also no possible moves.
     */
    @Test
    fun `colorHasPassed is set when player has no possible moves`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        // keine Block hat dann  keine möglichen Züge hat
        state.players.first().playerBlocks.clear()

        assertFalse(state.colorHasPassed[0])

        rootService.gameService.endTurn()

        //muss aktuelle colorHasPassed true sein da kein possible move gibt
        assertEquals(true, state.colorHasPassed.last())
    }

    /**
     * Tests that colorHasPassed is not set when player has possible moves.
     */
    @Test
    fun `colorHasPassed is not set when player has possible moves`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        state.players.forEach { it.playerBlocks.clear() }

        val block = Block(Color.BLUE, BlockType.O1)
        state.players.first().playerBlocks.add(block)

        assertFalse(state.colorHasPassed[0]) // start


        rootService.gameService.endTurn()

        val blueIndexAfter = state.colorOrder.indexOf(Color.BLUE)
        assertFalse(state.colorHasPassed[blueIndexAfter])
    }

    /** Tests that colorHasPassed is set when board is full so when player has not possible move .
    */
    @Test
    fun `colorHasPassed is set when board is full`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        // Brett komplett  voll dann keine possible move gibt
        for (i in 1 until 21) {
            for (j in 1 until 21) {
                state.gameBoard[i][j] = Color.RED
            }
        }


        val block = Block(Color.BLUE, BlockType.O1)
        block.tiles = mutableListOf(
            mutableListOf(Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL),
            mutableListOf(Color.ADJACENT, Color.BLUE,     Color.ADJACENT),
            mutableListOf(Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL)
        )
        state.players.first().playerBlocks.add(block) // player aht ein block aber keine platz im Brett gibt

        rootService.gameService.endTurn()

        assertTrue ( state.colorHasPassed.last())
    }

    /**
     * Tests that the original block is NOT mutated after checkForPossibleMove is called.
     * This is the key test to verify the fix works correctly.
     */
    @Test
    fun `original block is not mutated after checkForPossibleMove`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        // Blöcke für Spieler hinzufügen
        val block = Block(Color.BLUE, BlockType.O1)
        state.players.first().playerBlocks.add(block)

        // Zustand VOR dem Aufruf speichern
        val rotationBefore = block.rotation
        val mirrorBefore = block.isMirrored
        val tilesBefore = block.tiles.map { it.toMutableList() }.toMutableList()

        // endTurn ruft intern checkForPossibleMove auf
        rootService.gameService.endTurn()

        // Zustand NACH dem Aufruf prüfen -> muss gleich sein
        assertEquals(rotationBefore, block.rotation)
        assertEquals(mirrorBefore, block.isMirrored)
        assertEquals(tilesBefore, block.tiles)
    }
}




