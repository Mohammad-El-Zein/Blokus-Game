package service
import entity.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * RedoTest testet die Klasse Redo.
 * Diese Testklasse ist eine reine Kopie der UndoTest Klasse und hat nur kleine Anpassungen , die für Redo relevant
 * sind.
 */
class RedoTest {
    private lateinit var rootService : RootService

    /**
     * prepares the rootService and the testing environment before each test case runs
     */
    @BeforeTest
    fun setUp() {
        rootService = RootService()
    }
    // Grundlage schaffen : Spiel erstellen
    private fun testGame(){
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


    /**
     * Testfall 1
     */

    @Test
    fun `throw exception if no game exists`(){
        assertThrows<IllegalStateException> { rootService.playerActionService.redo()}
    }

    /**
     * Testfall 2
     */

    @Test
    fun `throw exception if game exists but no action or undo has been performed`(){
        testGame()
        assertThrows<IllegalStateException> { rootService.playerActionService.redo()}
    }

    /**
     * Testfall 3
     */
    @Test
    fun `gameState list should decrement if undo was performed and increment if redo was performed`(){
        testGame()
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]
        val firstPlayer = state.players.first()

        val firstPlayerBlock = firstPlayer.playerBlocks.first { it.blockType == BlockType.I5 }
        // I5 passend für Corner, Diagonal von I5 liegt bei Corner , Start bei 0,0 des Boards
        rootService.playerActionService.placeBlock(firstPlayerBlock,0,0)

        assertEquals(1,game.currentStateIndex)

        rootService.playerActionService.undo() // StateListe sollte jetzt 0 sein
        assertEquals(0,game.currentStateIndex)

        rootService.playerActionService.redo()
        assertEquals(1,game.currentStateIndex)
    }

    /**
     * Testfall 4
     */
    @Test
    fun `amount of redo ist equal to done undos`(){
        testGame()
        val game = checkNotNull(rootService.currentGame)

        val state = game.gameStates[game.currentStateIndex]
        val firstPlayer = state.players.first()
        val firstPlayerBlock = firstPlayer.playerBlocks.first { it.blockType == BlockType.I5 }
        // I5 passend für Corner, Diagonal von I5 liegt bei Corner , Start bei 0,0 des Boards
        rootService.playerActionService.placeBlock(firstPlayerBlock,0,0)
        // nach placeblock: currentStateIndex++, nächsten State beschreiben

        val state2 = game.gameStates[game.currentStateIndex] // nächster State, nächster Spieler
        val secondPlayer = state2.players.first()
        val secondPlayerBlock = secondPlayer.playerBlocks.first { it.blockType == BlockType.O1}
        // nächstmögliche Platzierung : z.B. 19,0 ( andere Corner oben rechts) nächster Spieler, nächste Corner
        rootService.playerActionService.placeBlock((secondPlayerBlock),19,0)



        assertEquals(2,game.currentStateIndex)

        rootService.playerActionService.undo()
        rootService.playerActionService.undo()
        assertEquals(0,game.currentStateIndex)

        // jetzt nicht mehr larger than zero es unmöglicher State bei nächstem undo
        assertThrows<IllegalStateException>{ rootService.playerActionService.undo()}


        rootService.playerActionService.redo()
        assertEquals(1,game.currentStateIndex)
        rootService.playerActionService.redo()
        assertEquals(2,game.currentStateIndex)

        assertThrows<IllegalStateException> {rootService.playerActionService.redo()}
    }

    /**
     * Testfall 5
     */
    @Test
    fun `exception if game State empty`(){
        testGame()
        val game = checkNotNull(rootService.currentGame)
        game.gameStates.clear()
        assertThrows<IllegalStateException> { rootService.playerActionService.redo()}
    }


}

