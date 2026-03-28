package service

import entity.*
import kotlin.test.*

/** a class that tests the functionality of the mirror function */
class MirrorTest {
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

    /** this test ensures that the tiles of a block get mirrored on the y-axis */
    @Test
    fun `a block should get mirrored correctly`(){
        val testBlock = Block(Color.RED, BlockType.T4)

        val col1 = mutableListOf(Color.RED, Color.BLANK, Color.BLUE)
        val col2 = mutableListOf(Color.GREEN, Color.YELLOW, Color.BLOCKED)
        val testMatrix = mutableListOf(col1, col2)

        testBlock.tiles = testMatrix

        val game = checkNotNull(rootService.currentGame) { "no active game found" }
        game.gameStates[0].players.first().playerBlocks.add(testBlock)

        rootService.playerActionService.mirror(testBlock)

        val goalCol1 = mutableListOf(Color.GREEN, Color.YELLOW, Color.BLOCKED)
        val goalCol2 = mutableListOf(Color.RED, Color.BLANK, Color.BLUE)
        val goalMatrix = mutableListOf(goalCol1, goalCol2)

        assertEquals(goalMatrix, testBlock.tiles)
    }

    /** this test confirms that mirroring a block updates it's isMirrored and rotation properties
     * to conform with network layer standards */
    @Test
    fun `the rotation and isMirrored statuses should get updated correctly`(){
        val testBlock = Block(Color.RED, BlockType.T4)


        val row1 = mutableListOf(Color.RED, Color.BLANK)
        val row2 = mutableListOf(Color.RED, Color.RED)
        val row3 = mutableListOf(Color.RED, Color.BLANK)
        val testMatrix = mutableListOf(row1, row2, row3)

        testBlock.tiles = testMatrix
        testBlock.rotation = Rotation.RIGHT
        testBlock.isMirrored = false

        val game = checkNotNull(rootService.currentGame) { "no active game found" }
        game.gameStates[0].players.first().playerBlocks.add(testBlock)

        rootService.playerActionService.mirror(testBlock)


        assertEquals(Rotation.LEFT, testBlock.rotation)
        assertEquals(true, testBlock.isMirrored)
    }
}