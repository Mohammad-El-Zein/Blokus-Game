package entity

import kotlin.test.*

/**test for the [BlokusGameState] class.*/

class BlokusGameStateTest {

    private fun createDefaultState() = BlokusGameState(
        players = mutableListOf(),
        gameMode = GameMode.FOUR_PLAYER,
        advancedScoring = true,
        sharedColor = Color.RED,
        colorOrder = mutableListOf()
    )

    /**
     * Tests that a BlokusGameState is created with correct default values
     */
    @Test
    fun `BlokusGameState is created correctly`() {
        val state = createDefaultState()

        assertEquals(GameMode.FOUR_PLAYER, state.gameMode)
        assertEquals(true, state.advancedScoring)
        assertEquals(Color.RED, state.sharedColor)
        assertEquals(0, state.sharedColorPlayerIndex)
        assertTrue(state.players.isEmpty())
        assertTrue(state.colorOrder.isEmpty())
        assertTrue(state.gameBoard.isEmpty())
    }

    /**
     * Tests that players can be added to the state
     */
    @Test
    fun `BlokusGameState players can be added`() {
        val state = createDefaultState()
        val player = Player("A", PlayerType.PLAYER)

        state.players.add(player)

        assertEquals(1, state.players.size)
        assertEquals("A", state.players[0].name)
    }


}