package service
import entity.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Prüfen, ob createPieces für verschiedene Spielmodi funktioniert.
 * 1. Jeder Spieler hat nach createGame genau 21 Steine ( bei 3 und 4 Spieler Modi).
 * 2. Bei 2 Spieler Modi mit 4 Farben hat jeder Spieler 42 Steine.
 * 3. Abbruch wenn Steine bereits existieren.
 */
class CreatePiecesTest {
    private lateinit var rootService: RootService

    /**
     * Init.
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
    }

    // Grundlage schaffen : Spiel erstellen
    private fun testGame() {
        rootService.gameService.createGame(
            isAdvancedScoring = false,
            players = mutableListOf(
                Triple("Player 1", Color.BLUE, PlayerType.PLAYER),
                Triple("Player 2", Color.YELLOW, PlayerType.PLAYER),
                Triple("Player 3", Color.RED, PlayerType.PLAYER),
                Triple("Player 4", Color.GREEN, PlayerType.PLAYER)
            ),
            gameMode = GameMode.FOUR_PLAYER
        )
    }

    private fun testGame_twoPlayer_fourColor() {
        rootService.gameService.createGame(
            isAdvancedScoring = false,
            players = mutableListOf(
                Triple("Player 1", Color.BLUE, PlayerType.PLAYER),
                Triple("Player 2", Color.YELLOW, PlayerType.PLAYER),
                Triple("Player 1", Color.RED, PlayerType.PLAYER),
                Triple("Player 2", Color.GREEN, PlayerType.PLAYER)
            ),
            gameMode = GameMode.TWO_PLAYER_FOUR_COLOR
        )
    }

    private fun testGame_threePlayer() {
        rootService.gameService.createGame(
            isAdvancedScoring = false,
            players = mutableListOf(
                Triple("Player 1", Color.BLUE, PlayerType.PLAYER),
                Triple("Player 2", Color.YELLOW, PlayerType.PLAYER),
                Triple("Player 3", Color.RED, PlayerType.PLAYER),
            ),
            gameMode = GameMode.THREE_PLAYER
        )
    }


    /**
     * Testfall 1.
     */
    @Test
    fun `every player hat 21 pieces in four player mode`() {
        testGame()
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        for (player in state.players) {
            assertEquals(21, player.playerBlocks.size)
        }
    }

    /**
     * Testfall 2.
     */
    @Test
    fun `in two player four color mode every player has 42 pieces`() {
        testGame_twoPlayer_fourColor()
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]
        for (player in state.players) {
            assertEquals(42, player.playerBlocks.size)
        }
    }

    /**
     * Testfall 3.
     */
    @Test
    fun `exception if pieces already exist`() {
        testGame()
        val game = checkNotNull(rootService.currentGame)
        assertThrows<IllegalStateException> {
            testGame()
        }
    }

    /**
     * Testfall 4 : Three Player , wegen sharedColor kommen für jeden Spieler zusätzlich 21 Steine dazu.
     */
    @Test
    fun `every player has 42 pieces in three player mode because of sharedColor`() {
        testGame_threePlayer()
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]
        for (player in state.players) {
            assertEquals(42, player.playerBlocks.size)

        }

    }
}