package service

import entity.*
import kotlin.test.*

/** a class that tests the functionality of the rotate function */
class RotateTest {
    /** rootService is initialized in setup -> late-init */
    private lateinit var rootService: RootService

    /**
     * Sets up the test environment before each test.
     * Creates a RootService with a TWO_PLAYER_FOUR_COLOR game and two players "A" and "B" .
     */
    @BeforeTest
    fun setUp(){
        rootService = RootService()
        val players = mutableListOf(Player("A", PlayerType.PLAYER), Player("B", PlayerType.PLAYER))
        val state = BlokusGameState (
            players = players,
            gameMode = GameMode.TWO_PLAYER_FOUR_COLOR,
            advancedScoring = false,
            sharedColor = Color.BLANK,
            colorOrder = mutableListOf(Color.BLUE, Color.YELLOW, Color.RED, Color.GREEN)
        )
        val game = BlokusGame(state)
        rootService.currentGame = game
    }

    /** this test ensures that the tiles of a block get rotated clockwise and that the
     * function works regardless of weather or not the tiles are a square matrix or a rectangular matrix */
    @Test
    fun `a block should get rotated correctly`(){
        val testBlock = Block(Color.RED, BlockType.T4)

        val col1 = mutableListOf(Color.RED, Color.BLANK)
        val col2 = mutableListOf(Color.RED, Color.RED)
        val col3 = mutableListOf(Color.RED, Color.BLANK)
        val testMatrix = mutableListOf(col1, col2, col3)


        testBlock.tiles = testMatrix

        val game = checkNotNull(rootService.currentGame) { "no active game found" }
        game.gameStates[0].players.first().playerBlocks.add(testBlock)

        rootService.playerActionService.rotate(testBlock)

        val goalCol1 = mutableListOf(Color.BLANK, Color.RED, Color.BLANK)
        val goalCol2 = mutableListOf(Color.RED,   Color.RED, Color.RED)
        val goalMatrix = mutableListOf(goalCol1, goalCol2)

        assertEquals(goalMatrix, testBlock.tiles)
    }

    /** this test ensures that the rotation property of a block is updated correctly
     * for the network layer */
    @Test
    fun `the rotation status should get updated correctly`(){
        val testBlock = Block(Color.RED, BlockType.T4)

        val row1 = mutableListOf(Color.RED, Color.BLANK)
        val row2 = mutableListOf(Color.RED, Color.RED)
        val row3 = mutableListOf(Color.RED, Color.BLANK)
        val testMatrix = mutableListOf(row1, row2, row3)


        testBlock.tiles = testMatrix

        val game = checkNotNull(rootService.currentGame) { "no active game found" }
        game.gameStates[0].players.first().playerBlocks.add(testBlock)

        rootService.playerActionService.rotate(testBlock)

        assertEquals(Rotation.RIGHT, testBlock.rotation)
    }
}