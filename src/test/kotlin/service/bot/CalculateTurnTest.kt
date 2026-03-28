package service.bot

import entity.BlokusGame
import entity.BlokusGameState
import entity.Color
import entity.GameMode
import entity.Player
import entity.PlayerType
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import service.RootService
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Test for function calculateTurn of BotService
 * @property rootService: RootService instance
 * @property botService: BotService instance
 */
class CalculateTurnTest {
    lateinit var rootService: RootService
    lateinit var botService: BotService

    /**
     * Setup for tests
     */
    @BeforeTest
    fun setup() {
        rootService = RootService()
        botService = BotService(rootService)
    }

    /**
     *[`throws exception if game is null`] self explanatory title
     */
    @Test
    fun `throws exception if game is null`() {
        rootService.currentGame = null
        val exception = assertThrows<IllegalStateException> {
            botService.calculateTurn()
        }
        assertEquals(
            "Game is null", exception.message,
            "should throw exception game is null"
        )
    }

    /**
     * [`when playerType is player then does nothing`]
     * because it's called in game service and needs to check if the player is
     * bot or human
     */
    @Test
    fun `when playerType is player then does nothing`(){
        val players = mutableListOf(
            Player("Beyonce", PlayerType.PLAYER),
            Player("Madonna", PlayerType.PLAYER)
        )

        val bstate = BlokusGameState(
            players, GameMode.TWO_PLAYER_SMALL,
            false, Color.BLUE,
            mutableListOf(Color.RED, Color.GREEN, Color.YELLOW)
        )
        rootService.currentGame = BlokusGame(bstate)
        assertNotNull(rootService.currentGame)

        val result = botService.calculateTurn()
        assertEquals(
            Unit, result, "Nothing should change" +
                    "if players arent bots"
        )

        assertDoesNotThrow { botService.calculateTurn() } // Does not enter
    }}