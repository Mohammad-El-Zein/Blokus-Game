package service

import entity.*
import kotlin.test.*

/** Tests for nextPlayer() and getCurrentPlayer() methods in GameService. */
class NextPlayerTest {

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

        state.colorHasPassed = MutableList(4) { false } // alle color nicht gepasst
        val game = BlokusGame(state)
        rootService.currentGame = game
    }

    /**
     * Tests that nextPlayer()  should rotate players correctly in FOUR_PLAYER mode.
     */
    @Test
    fun `nextPlayer should  rotates players correctly in FOUR_PLAYER mode`() {

        rootService.gameService.nextPlayer()
        assertEquals("Player_B", rootService.gameService.getCurrentPlayer().name)

        rootService.gameService.nextPlayer()
        assertEquals("Player_C", rootService.gameService.getCurrentPlayer().name)

        rootService.gameService.nextPlayer()
        assertEquals("Player_D", rootService.gameService.getCurrentPlayer().name)

        rootService.gameService.nextPlayer()
        assertEquals("Player_A", rootService.gameService.getCurrentPlayer().name)

        rootService.gameService.nextPlayer()
        assertEquals("Player_B", rootService.gameService.getCurrentPlayer().name)




    }

    /**
     * Tests that nextPlayer() rotates colorOrder correctly.
     */
    @Test
    fun `nextPlayer  should rotates colorOrder correctly`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        assertEquals(Color.BLUE, state.colorOrder.first())

        rootService.gameService.nextPlayer()
        assertEquals(Color.YELLOW, state.colorOrder.first())

        rootService.gameService.nextPlayer()
        assertEquals(Color.RED, state.colorOrder.first())

        rootService.gameService.nextPlayer()
        assertEquals(Color.GREEN, state.colorOrder.first())

        rootService.gameService.nextPlayer()
        assertEquals(Color.BLUE, state.colorOrder.first())
    }

    /**
    * Tests that nextPlayer() rotates colorHasPassed correctly.
    */
    @Test
    fun `nextPlayer should rotate colorHasPassed correctly`() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        // erste Farbe hat passed setzen
        state.colorHasPassed[0] = true

        rootService.gameService.nextPlayer()

        // nach nextPlayer aufrufen muss Rotation colorHasSassed am Ende setzen
        assertEquals(true, state.colorHasPassed.last())
        assertEquals(false, state.colorHasPassed.first())
    }

    /**
     * Tests that getCurrentPlayer() returns the first player at game start.
     */
    @Test
    fun `getCurrentPlayer returns first player at game start`() {
        val currentPlayer = rootService.gameService.getCurrentPlayer()
        assertEquals("Player_A", currentPlayer.name)
    }

    /**
     * Tests that getCurrentPlayer() throws exception when no game is active.
     */
    @Test
    fun `getCurrentPlayer throws exception when no active game`() {
        rootService.currentGame = null
        assertFailsWith<IllegalStateException> {
            rootService.gameService.getCurrentPlayer()
        }
    }

    /**
     * Tests that nextPlayer() works correctly in THREE_PLAYER mode.
     * should new setUp do , so create a RootService with a THREE_PLAYER gameMode and three players A , B, C.
     *
     */
    @Test
    fun `nextPlayer  should  correctly walk in THREE_PLAYER mode`() {
        val players = mutableListOf(                 // neue Spiel mit THREE_PLAYER aufzusetzen
            Player("Player_A", PlayerType.PLAYER),
            Player("Player_B", PlayerType.PLAYER),
            Player("Player_C", PlayerType.PLAYER)
        )
        val state = BlokusGameState(
            players = players,
            gameMode = GameMode.THREE_PLAYER,
            advancedScoring = false,
            sharedColor = Color.GREEN,
            colorOrder = mutableListOf(Color.BLUE, Color.YELLOW, Color.RED, Color.GREEN)
        )
        state.colorHasPassed = MutableList(4) { false }

        val game = BlokusGame(state)
        rootService.currentGame = game

        println(game.currentStateIndex)
        println(game.blokusGameState.sharedColor)
        println(game.blokusGameState.colorOrder)
        println(game.gameStates[game.currentStateIndex])
        assertEquals("Player_A", rootService.gameService.getCurrentPlayer().name)

        rootService.gameService.nextPlayer()
        assertEquals("Player_B", rootService.gameService.getCurrentPlayer().name)

        rootService.gameService.nextPlayer()
        assertEquals("Player_C", rootService.gameService.getCurrentPlayer().name)

        rootService.gameService.nextPlayer()
        assertEquals("Player_A", rootService.gameService.getCurrentPlayer().name)



    }

    /**
     * Tests that nextPlayer() rotates colorOrder correctly in TWO_PLAYER_FOUR_COLOR mode.
     */
    @Test
    fun `nextPlayer rotates colorOrder correctly in TWO_PLAYER_FOUR_COLOR mode`() {
        val players = mutableListOf(
            Player("Player_A", PlayerType.PLAYER),
            Player("Player_B", PlayerType.PLAYER)
        )
        val state = BlokusGameState(
            players = players,
            gameMode = GameMode.TWO_PLAYER_FOUR_COLOR,
            advancedScoring = false,
            sharedColor = Color.BLANK,
            colorOrder = mutableListOf(Color.BLUE, Color.YELLOW, Color.RED , Color.GREEN)
        )
        state.colorHasPassed = MutableList(4) { false }

        val game = BlokusGame(state)
        rootService.currentGame = game

        assertEquals("Player_A", rootService.gameService.getCurrentPlayer().name)
        assertEquals(Color.BLUE, state.colorOrder.first())

        rootService.gameService.nextPlayer()
        assertEquals("Player_B", rootService.gameService.getCurrentPlayer().name)
        assertEquals(Color.YELLOW, state.colorOrder.first())
        assertEquals(Color.BLUE, state.colorOrder.last())

        rootService.gameService.nextPlayer() // nochmal spieler A da spieler A muss Blue und Red hat
        assertEquals(Color.RED, state.colorOrder.first())
        assertEquals("Player_A", rootService.gameService.getCurrentPlayer().name)

        rootService.gameService.nextPlayer() // nochmal spieler A da spieler A muss Blue und Red hat
        assertEquals(Color.GREEN, state.colorOrder.first())
        assertEquals("Player_B", rootService.gameService.getCurrentPlayer().name)


    }

    /**
     * Tests that nextPlayer() rotates colorOrder correctly in TWO_PLAYER_SMALL mode.
     */
    @Test
    fun `nextPlayer rotates colorOrder correctly in TWO_PLAYER_SMALL mode`() {
        val players = mutableListOf(
            Player("Player_A", PlayerType.PLAYER),
            Player("Player_B", PlayerType.PLAYER)
        )
        val state = BlokusGameState(
            players = players,
            gameMode = GameMode.TWO_PLAYER_SMALL,
            advancedScoring = false,
            sharedColor = Color.BLANK,
            colorOrder = mutableListOf(Color.BLUE, Color.YELLOW)
        )
        state.gameBoard = MutableList(16) { MutableList(16) { Color.BLANK } }
        state.colorHasPassed = MutableList(2) { false }

        val game = BlokusGame(state)
        rootService.currentGame = game

        assertEquals(Color.BLUE, state.colorOrder.first())

        rootService.gameService.nextPlayer()
        assertEquals("Player_B", rootService.gameService.getCurrentPlayer().name)
        assertEquals(Color.YELLOW, state.colorOrder.first())
        assertEquals(Color.BLUE, state.colorOrder.last())

        rootService.gameService.nextPlayer()
        assertEquals("Player_A", rootService.gameService.getCurrentPlayer().name)
        assertEquals(Color.BLUE, state.colorOrder.first())
    }

    /**
     * Tests that nextPlayer() throws exception when no game is active.
     */
    @Test
    fun `nextPlayer throws exception when no active game`() {
        rootService.currentGame = null
        assertFailsWith<IllegalStateException> {
            rootService.gameService.nextPlayer()
        }
    }

    /**
     * tests if the current player is moved to the end of the list after their turn
     */
    @Test
    fun `nextPlayer puts player in end of player list if color is not shared`(){
        val players = mutableListOf(                 // neue Spiel mit THREE_PLAYER aufzusetzen
            Player("Player_A", PlayerType.PLAYER),
            Player("Player_B", PlayerType.PLAYER),
            Player("Player_C", PlayerType.PLAYER)
        )
        val state = BlokusGameState(
            players = players,
            gameMode = GameMode.THREE_PLAYER,
            advancedScoring = false,
            sharedColor = Color.YELLOW,
            colorOrder = mutableListOf(Color.BLUE, Color.RED, Color.GREEN,Color.YELLOW)
        )
        state.colorHasPassed = MutableList(4) { false }
        state.sharedColorPlayerIndex = state.colorOrder.indexOf(state.colorOrder.last())
        val game = BlokusGame(state)
        rootService.currentGame = game

        rootService.gameService.nextPlayer()

        assertEquals("Player_A", game.blokusGameState.players.last().name,
            "shared color is different than first order so player should change")
    }





}