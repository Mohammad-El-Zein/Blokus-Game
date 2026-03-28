package service
import entity.*
import kotlin.test.*
import java.io.File

/** Tests for the saveGame() and loadGame() methods in GameService.*/
class SaveGameAndLoadGameTest {
    /**
     * Test that saveGame throws an IllegalStateException if no active game is present.
     */
    @Test
    fun `saveGame should throw exception if no active game found`() {
        val rootService = RootService()

        assertFailsWith<IllegalStateException>("no active game found") {
            rootService.gameService.saveGame("Test")
        }
    }

    /**
     * Test that loadGame throws an IllegalStateException if The game file doesn't exist.
     */
    @Test
    fun `loadGame should throw exception if the game file doesn't exist`() {
        val rootService = RootService()
        assertFailsWith<IllegalStateException>("The game file doesn't exist") {
            rootService.gameService.loadGame("NotExist.json")
        }
    }

    /**
     * Verifies that a game is correctly saved to a file and restored using loadGame.
     */
    @Test
    fun `saveGame and loadGame should work`() {
        val rootService = RootService()

        rootService.gameService.createGame(true, listOf(
            Triple("A", Color.YELLOW, PlayerType.PLAYER),
            Triple("B", Color.BLUE, PlayerType.PLAYER)),
            GameMode.TWO_PLAYER_SMALL
        )

        rootService.gameService.saveGame("TestSaveAndLoad")

        rootService.currentGame = null

        rootService.gameService.loadGame("TestSaveAndLoad")

        val game = checkNotNull(rootService.currentGame)
        assertEquals(Color.YELLOW, game.gameStates[game.currentStateIndex].colorOrder[0])
        assertEquals(Color.BLUE, game.gameStates[game.currentStateIndex].colorOrder[1])
        assertEquals("A", game.gameStates[game.currentStateIndex].players[0].name)
        assertEquals("B", game.gameStates[game.currentStateIndex].players[1].name)

        assertEquals(GameMode.TWO_PLAYER_SMALL, game.gameStates[game.currentStateIndex].gameMode)

        assertEquals(1, game.gameStates.size)
        assertEquals(21, game.gameStates[0].players[0].playerBlocks.size)
        File("TestSaveAndLoad.json").delete()
    }
}
