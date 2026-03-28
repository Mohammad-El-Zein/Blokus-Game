package entity

import kotlin.test.*

/**test for the [Player] class.*/

class PlayerTest {

    /**
     * Tests that a Player is created with correct default values
     */
    @Test
    fun `Player is created correctly`() {
        val player = Player("BOT 1", PlayerType.TESTBOT)

        assertEquals("BOT 1", player.name)
        assertEquals(PlayerType.TESTBOT, player.playerType)
        assertEquals(0, player.score)
        assertTrue(player.playerBlocks.isEmpty())
    }



    /**
     * Tests that blocks can be added to the player
     */
    @Test
    fun `Player blocks can be added`() {
        val player = Player("A", PlayerType.PLAYER)
        val block = Block(Color.BLUE, BlockType.O1)

        player.playerBlocks.add(block)

        assertEquals(1, player.playerBlocks.size)
        assertEquals(Color.BLUE, player.playerBlocks[0].color)
    }

    /**
     * Tests that different PlayerTypes can be assigned
     */
    @Test
    fun `Player can have different PlayerTypes`() {
        val human = Player("A", PlayerType.PLAYER)
        val testBot = Player("BOT 1", PlayerType.TESTBOT)
        val stockFishWannabe = Player("BOT 2", PlayerType.STOCKFISHWANNABE)

        assertEquals(PlayerType.PLAYER, human.playerType)
        assertEquals(PlayerType.TESTBOT, testBot.playerType)
        assertEquals(PlayerType.STOCKFISHWANNABE, stockFishWannabe.playerType)
    }


}