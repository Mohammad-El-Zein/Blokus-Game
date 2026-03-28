package service
import entity.*
import kotlin.test.*

/** Tests for the validatePlacement() methods in GameService.*/
class ValidatePlacementTest {
    private lateinit var rootService: RootService

    /**
     * Sets up the test environment before each test.
     * Creates a RootService with a TWO_PLAYER_SMALL gameMode and two players A , B.
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()

        rootService.gameService.createGame(true, listOf(
            Triple("A", Color.YELLOW, PlayerType.PLAYER),
            Triple("B", Color.BLUE, PlayerType.PLAYER)),
            GameMode.TWO_PLAYER_SMALL
        )
    }

    /**
     * Tests that validatePlacement() throws an IllegalArgumentException if x or y is out of bounds.
     */
    @Test
    fun `Tests that validatePlacement throws an IllegalArgumentException if x or y is out of bounds`() {
        assertFailsWith<IllegalArgumentException>("x-coordinate is out of bounds") {
            rootService.gameService.validatePlacement(-1,2, Block(Color.BLUE, BlockType.O1))
        }

        assertFailsWith<IllegalArgumentException>("y-coordinate is out of bounds") {
            rootService.gameService.validatePlacement(1,-2, Block(Color.BLUE, BlockType.O1))
        }
    }

    /**
     * Tests that validatePlacement() checks the first move correctly.
     */
    @Test
    fun `Tests that validatePlacement checks the first move correctly`() {
        assertTrue(
            rootService.gameService.validatePlacement(0,0, Block(Color.BLUE, BlockType.O1))
        )
        assertTrue(
            rootService.gameService.validatePlacement(13,0, Block(Color.BLUE, BlockType.O1))
        )
        assertTrue(
            rootService.gameService.validatePlacement(0,13, Block(Color.BLUE, BlockType.O1))
        )
        assertTrue(
            rootService.gameService.validatePlacement(13,13, Block(Color.BLUE, BlockType.O1))
        )

        assertFalse(
            rootService.gameService.validatePlacement(10,10, Block(Color.BLUE, BlockType.O1))
        )
    }

    /**
     * Tests that validatePlacement() checks normal case correctly.
     */
    @Test
    fun `Tests that validatePlacement checks normal case correctly`() {

        val o1BlockFirstPlayer =rootService.gameService.getCurrentPlayer().playerBlocks.filter {
            it.blockType == BlockType.O1 }[0]
        rootService.playerActionService.placeBlock(o1BlockFirstPlayer, 0, 0)

        val o1BlockSecondPlayer =rootService.gameService.getCurrentPlayer().playerBlocks.filter {
            it.blockType == BlockType.O1 }[0]
        rootService.playerActionService.placeBlock(o1BlockSecondPlayer, 13, 13)


        assertTrue(
            rootService.gameService.validatePlacement(1,1, Block(Color.YELLOW, BlockType.V5))
        )

        assertTrue(
            rootService.gameService.validatePlacement(11,11, Block(Color.BLUE, BlockType.O4))
        )

        assertFalse(
            rootService.gameService.validatePlacement(0,0, Block(Color.YELLOW, BlockType.V5))
        )

        assertFalse(
            rootService.gameService.validatePlacement(0,0, Block(Color.BLUE, BlockType.O4))
        )

        assertFalse(
            rootService.gameService.validatePlacement(
                12,
                12,
                Block(Color.YELLOW, BlockType.O1)
            )
        )

        assertFalse(
            rootService.gameService.validatePlacement(0,1, Block(Color.YELLOW, BlockType.V5))
        )

        assertFalse(
            rootService.gameService.validatePlacement(12,11, Block(Color.BLUE, BlockType.L5))
        )
    }
}