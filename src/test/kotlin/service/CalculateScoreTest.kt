package service
import entity.*
import kotlin.test.*

/** Tests for the calculateScore() methods in GameService.*/
class CalculateScoreTest {
    private lateinit var rootService: RootService

    /**
     * Sets up the test environment before each test.
     * Creates a RootService with a TWO_PLAYER_FOUR_COLOR gameMode and two players A , B.
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()

        rootService.gameService.createGame(false, listOf(
            Triple("A", Color.BLUE, PlayerType.PLAYER),
            Triple("B", Color.YELLOW, PlayerType.PLAYER),
            Triple("A", Color.RED, PlayerType.PLAYER),
            Triple("B", Color.GREEN, PlayerType.PLAYER)),
            GameMode.TWO_PLAYER_FOUR_COLOR
        )
    }

    /**
     * Tests that calculateScore() throws an IllegalStateException if the game is still running.
     */
    @Test
    fun `Tests that calculateScore throws an IllegalStateException if the game is still running`() {
        assertFailsWith<IllegalStateException>("The game is still running") {
            rootService.gameService.calculateScore()
        }
    }

    /**
     * Tests that calculateScore() works correctly for basic scoring.
     */
    @Test
    fun `Test for simple basic scoring`() {
        val game = checkNotNull(rootService.currentGame)
        game.gameStates[game.currentStateIndex].colorHasPassed = mutableListOf(true, true, true, true)

        rootService.gameService.calculateScore()

        assertTrue { game.gameStates[game.currentStateIndex].players.all { it.score == 178 } }
    }

    /**
     * Tests that calculateScore() works correctly for advanced scoring.
     */
    @Test
    fun `Test for advanced scoring`() {
        rootService.currentGame = null
        rootService.gameService.createGame(true, listOf(
            Triple("A", Color.BLUE, PlayerType.PLAYER),
            Triple("B", Color.YELLOW, PlayerType.PLAYER),
            Triple("A", Color.RED, PlayerType.PLAYER),
            Triple("B", Color.GREEN, PlayerType.PLAYER)),
            GameMode.TWO_PLAYER_FOUR_COLOR
        )
        val game = checkNotNull(rootService.currentGame)

        game.gameStates[game.currentStateIndex].players[0].playerBlocks.removeAll(
            game.gameStates[game.currentStateIndex].players[0].playerBlocks.filterNot {
                it.blockType == BlockType.O1 }
        )

        game.gameStates[game.currentStateIndex].players[1].playerBlocks.removeAll(
            game.gameStates[game.currentStateIndex].players[1].playerBlocks.filterNot {
                it.blockType == BlockType.O4 }
        )


        var o1BlockFirstPlayer =rootService.gameService.getCurrentPlayer().playerBlocks.filter {
            it.blockType == BlockType.O1 }[0]
        rootService.playerActionService.placeBlock(o1BlockFirstPlayer, 0, 0)

        var o4BlockSecondPlayer =rootService.gameService.getCurrentPlayer().playerBlocks.filter {
            it.blockType == BlockType.O4 }[0]
        rootService.playerActionService.placeBlock(o4BlockSecondPlayer, 18, 18)

        o1BlockFirstPlayer =rootService.gameService.getCurrentPlayer().playerBlocks.filter {
            it.blockType == BlockType.O1 }[0]
        rootService.playerActionService.placeBlock(o1BlockFirstPlayer, 19, 0)

        o4BlockSecondPlayer =rootService.gameService.getCurrentPlayer().playerBlocks.filter {
            it.blockType == BlockType.O4 }[0]
        rootService.playerActionService.placeBlock(o4BlockSecondPlayer, 0, 18)

        game.gameStates[game.currentStateIndex].colorHasPassed = mutableListOf(true, true, true, true)

        rootService.gameService.calculateScore()

        val playerA = game.gameStates[game.currentStateIndex].players.first { it.name == "A" }
        val playerB = game.gameStates[game.currentStateIndex].players.first { it.name == "B" }

        assertEquals(40, playerA.score)
        assertEquals(30, playerB.score)
    }
}