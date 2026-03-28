package service

import entity.*
import kotlin.test.*

/** Tests for the placeBlock() method in PlayerActionService. */
class PlaceBlockTest {

    private lateinit var rootService: RootService

    /**
     * Sets up the test environment before each test.
     * Creates a RootService with a FOUR_PLAYER game, four players(A,B,C,D) and a game board.
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
        state.gameBoard = MutableList(22) { MutableList(22) { Color.BLANK } }

        // initiale board initialisieren wie aus creatBoardGame
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


    private fun createO1Block(): Block {
        return Block(Color.BLUE, BlockType.O1)

    }

    /**
     * Tests that placeBlock() throws exception when no game is active.
     */
    @Test
    fun `placeBlock throws exception when no active game`() {
        val block = createO1Block()
        rootService.currentGame = null

        assertFailsWith<IllegalStateException> {
            rootService.playerActionService.placeBlock(block, 0, 0)
        }
    }

    /**
     * Tests that placeBlock() throws exception when placement is invalid.
     */
    @Test
    fun `placeBlock throws exception when placement is invalid`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]
        val block = createO1Block()

        // Block dem Spieler geben
        state.players.first().playerBlocks.add(block)

        // ungültige Position  da in  mitten auf Brett keine Ecke gibt
        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.placeBlock(block, 10, 10)
        }
    }

    /**
     * Tests that placeBlock() places the block correctly on the board.
     */
    @Test
    fun `placeBlock places block correctly on board`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]
        val block = createO1Block()

        state.players.first().playerBlocks.add(block)

        // Block in ecke legen also richtige postion
        rootService.playerActionService.placeBlock(block, 0, 0)



        val newState = game.gameStates[game.currentStateIndex]
        assertEquals(Color.BLUE, newState.gameBoard[1][1])//[1][1] da 22x22 unsere board size ist
    }

    /**
     * Tests that placeBlock() places the block correctly on the board.
     */
    @Test
    fun `placeBlock places V3 block correctly on board`() { // zb andere test bei v3
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        val block = Block(Color.BLUE, BlockType.V3) // v3 aus block aufrufen
        state.players.first().playerBlocks.add(block)

        rootService.playerActionService.placeBlock(block, 0, 0)

        val newState = game.gameStates[game.currentStateIndex]
        assertEquals(Color.BLUE, newState.gameBoard[1][1])
        assertEquals(Color.BLUE, newState.gameBoard[2][1])
        assertEquals(Color.BLUE, newState.gameBoard[2][2])
    }

    /**
     * Tests that placeBlock() removes block from player's hand after placing.
     */
    @Test
    fun `placeBlock removes block from player hand`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]
        val block = createO1Block()

        state.players.first().playerBlocks.add(block)
        assertEquals(1, state.players.first().playerBlocks.size)

        rootService.playerActionService.placeBlock(block, 0, 0)

        // Block wurde aus der Hand entfernt
        val newState = game.gameStates[game.currentStateIndex]
        assertEquals(0, newState.players.first().playerBlocks.size)
    }

    /**
     * Tests that placeBlock() throws exception when placement is outside the board size.
     */
    @Test
    fun `placeBlock throws exception when placement is outside the board`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        val block = createO1Block()
        state.players.first().playerBlocks.add(block)

        assertFailsWith<IllegalArgumentException> {
            rootService.playerActionService.placeBlock(block, 30, 30)
        }
    }

    /**
     * Tests that placeBlock() adds a new state to history.
     */
    @Test
    fun `placeBlock adds new state to history`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]
        val block = createO1Block()

        state.players.first().playerBlocks.add(block)

        assertEquals(1, game.gameStates.size)

        rootService.playerActionService.placeBlock(block, 0, 0)

        assertEquals(2, game.gameStates.size)
        assertEquals(1, game.currentStateIndex)
    }

    /**
     * Tests that placeBlock() removes shared color block from all players in THREE_PLAYER mode.
     * also every player has 42 pieces at the start (21 own + 21 sharedColor).
     */
    @Test
    fun `placeBlock removes sharedColor block from all players in THREE_PLAYER mode`() {
        rootService.currentGame = null
        rootService.gameService.createGame(
            isAdvancedScoring = false,
            players = listOf(
                Triple("Player_A", Color.BLUE, PlayerType.PLAYER),
                Triple("Player_B", Color.YELLOW, PlayerType.PLAYER),
                Triple("Player_C", Color.RED, PlayerType.PLAYER)
            ),
            gameMode = GameMode.THREE_PLAYER
        )

        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]
        val sharedColor = state.sharedColor

        // muss jeder Spieler 42 Steine haben (21 eigene + 21 sharedColor)
        state.players.forEach { player ->
            assertEquals(42, player.playerBlocks.size)
        }

        // muss jeder Spieler genau 21 sharedColor Steine haben
         state.players.forEach { player ->
            assertEquals(21, player.playerBlocks.count { it.color == sharedColor })
        }

        // nach sharedColor Runde  gehen
        while (state.colorOrder.first() != sharedColor) {
            rootService.gameService.nextPlayer()
        }

        val currentPlayer = rootService.gameService.getCurrentPlayer()
        val sharedBlock = currentPlayer.playerBlocks.first { it.color == sharedColor }

        // shared color Block legen
        rootService.playerActionService.placeBlock(sharedBlock, 0, 0)

        // nach platzierung muss jetzt alle Spieler 41 Steine haben (42 - 1 sharedColor Stein)
        val newState = game.gameStates[game.currentStateIndex]
        newState.players.forEach { player ->
            assertEquals(41, player.playerBlocks.size)
        }

        // auch muss alle Spieler jetzt 20 sharedColor Steine haben
         newState.players.forEach { player ->
            assertEquals(20, player.playerBlocks.count { it.color == sharedColor })
        }
    }




}