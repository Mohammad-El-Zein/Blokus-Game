package service
import entity.Color
import entity.GameMode
import entity.PlayerType
import org.junit.jupiter.api.assertThrows
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Prüfen, ob create Game funktioniert:
 * 1. createGame erstellt ein Spiel
 * 2. Wenn Spiel bereits läuft, dann exception.
 * 3. Fehler bei falscher Spieleranzahl.
 * 4. Fehler bei falscher colorOrder Größe.
 */
class CreateGameTest {
    private lateinit var rootService : RootService

    /**
     * Initialisierung.
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
    }

    /**
     * Erster Testfall
     */
    @Test
    fun `game created after createGame`(){
        rootService.gameService.createGame(
            isAdvancedScoring = false,
            players = mutableListOf(
                Triple("Player 1",Color.BLUE, PlayerType.PLAYER),
                Triple("Player 2",Color.YELLOW, PlayerType.PLAYER),
                Triple("Player 3",Color.RED, PlayerType.PLAYER),
                Triple("Player 4",Color.GREEN, PlayerType.PLAYER)
            ),
            gameMode = GameMode.FOUR_PLAYER

        )

        assertNotNull(rootService.currentGame)
    }

    /**
     * Zweiter Testfall
     */
    @Test
    fun ` throw exception if game already exists`(){
        rootService.gameService.createGame(
            isAdvancedScoring = false,
            players = mutableListOf(
                Triple("Player 1",Color.BLUE, PlayerType.PLAYER),
                Triple("Player 2",Color.YELLOW, PlayerType.PLAYER),
                Triple("Player 3",Color.RED, PlayerType.PLAYER),
                Triple("Player 4",Color.GREEN, PlayerType.PLAYER)
            ),
            gameMode = GameMode.FOUR_PLAYER
        )
        assertThrows<IllegalStateException> {
            rootService.gameService.createGame(
                isAdvancedScoring = false,
                players = mutableListOf(
                    Triple("Player 1",Color.BLUE, PlayerType.PLAYER),
                    Triple("Player 2",Color.YELLOW, PlayerType.PLAYER),
                    Triple("Player 3",Color.RED, PlayerType.PLAYER),
                    Triple("Player 4",Color.GREEN, PlayerType.PLAYER)
                ),
                gameMode = GameMode.FOUR_PLAYER
            )
        }
    }

    /**
     * Testfall 3
     */
    @Test
    fun `throw Exception if number of players is not valid`() {
        assertThrows<IllegalArgumentException> {
            rootService.gameService.createGame(
                isAdvancedScoring = false,
                players = mutableListOf(
                    Triple("Player 1", Color.BLUE, PlayerType.PLAYER),
                    Triple("Player 2", Color.YELLOW, PlayerType.PLAYER),
                    Triple("Player 3", Color.RED, PlayerType.PLAYER),
                    Triple("Player 4", Color.GREEN, PlayerType.PLAYER)
                ),
                gameMode = GameMode.THREE_PLAYER // falsches Argument
            )
        }
    }


    /**
     * Testfall 5: THREE_PLAYER
     */
    @Test
    fun `game created in THREE_PLAYER mode`() {
        rootService.gameService.createGame(
            isAdvancedScoring = false,
            players = mutableListOf(
                Triple("Player 1", Color.BLUE, PlayerType.PLAYER),
                Triple("Player 2", Color.YELLOW, PlayerType.PLAYER),
                Triple("Player 3", Color.RED, PlayerType.PLAYER)
            ),
            gameMode = GameMode.THREE_PLAYER
        )
        assertNotNull(rootService.currentGame)
    }

    /**
     * Testfall 6: TWO_PLAYER_SMALL
     */
    @Test
    fun `game created in TWO_PLAYER_SMALL mode`() {
        rootService.gameService.createGame(
            isAdvancedScoring = false,
            players = mutableListOf(
                Triple("Player 1", Color.BLUE, PlayerType.PLAYER),
                Triple("Player 2", Color.YELLOW, PlayerType.PLAYER)
            ),
            gameMode = GameMode.TWO_PLAYER_SMALL
        )
        assertNotNull(rootService.currentGame)
    }

    /**
     * Testfall 7: TWO_PLAYER_FOUR_COLOR
     */
    @Test
    fun `game created in TWO_PLAYER_FOUR_COLOR mode`() {
        rootService.gameService.createGame(
            isAdvancedScoring = false,
            players = mutableListOf(
                Triple("Player 1", Color.BLUE, PlayerType.PLAYER),
                Triple("Player 2", Color.YELLOW, PlayerType.PLAYER)
            ),
            gameMode = GameMode.TWO_PLAYER_FOUR_COLOR
        )
        assertNotNull(rootService.currentGame)
    }



}