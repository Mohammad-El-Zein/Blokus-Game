package entity

import kotlin.test.*


/**test for the [BlokusGame] class.*/
class BlokusGameTest {


    private fun createDefaultState() = BlokusGameState(
        players = mutableListOf(),
        gameMode = GameMode.FOUR_PLAYER,
        advancedScoring = false,
        sharedColor = Color.RED,
        colorOrder = mutableListOf()
    )

    /**
     * Tests that a BlokusGame is created with correct default values
     */
    @Test
    fun `Blokus Game is created correctly`() {
        val game = BlokusGame(createDefaultState())

        assertEquals(0, game.currentStateIndex)
        assertEquals(1, game.gameStates.size)
    }

    /**
     * Tests that history can be filled with BlokusGameStates
     */
    @Test
    fun testAddToHistory() {
        val game = BlokusGame(createDefaultState())

        game.gameStates.add(createDefaultState())

        assertEquals(2, game.gameStates.size)
    }

    /**
     * Tests that currentStateIndex can be changed
     */
    @Test
    fun testCurrentStateIndex() {
        val game = BlokusGame(createDefaultState())

        game.currentStateIndex = 2

        assertEquals(2, game.currentStateIndex)
    }

}