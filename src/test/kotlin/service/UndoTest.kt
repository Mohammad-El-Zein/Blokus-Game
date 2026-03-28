package service

import entity.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Benötigt werden 4 Testfälle :
 * 1. Undo funktioniert nicht ohne ein laufendes Spiel.
 * 2. Bei einem laufenden Spiel ohne bisher ausgeführt Aktion funktioniert undo auch nicht.
 * 3. Undo funktioniert bi einem laufenden Spiel wie gewohnt:
 * --> Index wird dekrementiert und Spieler bekommt seinen Block wieder.
 * 4. Mehrfaches Undo möglich.
 * 5. GameState sind leer , aber currentStateIndex ist gültig, exception.

 */
class UndoTest {

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
        assertThrows<IllegalStateException> { rootService.playerActionService.undo()}
    }

    /** Testfall 2 :  Ab jetzt Spiel erstellen*/
    @Test
    fun `throw exception if game exists but no action has been performed`(){
        testGame()
        assertThrows<IllegalStateException> { rootService.playerActionService.undo()}
    }

    /**Testfall 3*/
     @Test
    fun `gameState list should decrement if undo was performed`(){
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
    }

    /** Testfall 4*/
    @Test
    fun `undo is possible as long as stateIndex is larger than zero`(){
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
    }

    /**
     * Testfall 5
     */
    @Test
    fun `when gameState empty but index is valid throw exception`(){
        testGame()
        val game = checkNotNull(rootService.currentGame)
        game.currentStateIndex = 1
        game.gameStates.clear()
        assertThrows<IllegalStateException>{ rootService.playerActionService.undo()}
    }


}