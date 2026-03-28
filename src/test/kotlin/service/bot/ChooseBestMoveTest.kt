package service.bot

import entity.BlockType
import entity.Color
import entity.GameMode
import entity.PlayerType
import service.RootService
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * this class tests the "smart bot" (STOCKFISHWANNABE) logic and it makes sure the bot follows the"Greedy Strategy"
 * by always picking the largest available block to get the best score
 *
 * @property rootService the main service used to set up the game environment
 * @property botService the service that contains the bot´s thinking logic
 * */
class ChooseBestMoveTest {
    private lateinit var rootService: RootService
    private lateinit var botService: BotService


    /**
     * sets up the RootService and BotService before each test runs
     */
    @BeforeTest
    fun setUp(){
        rootService = RootService()
        botService = rootService.botService
    }


    /**
     * helper fun that starts a standard 4p game, and it sets one bot and three huma players to creat a real game state
     */
    private fun create4PGame(){
        rootService.gameService.createGame(
            isAdvancedScoring = false,
            players = listOf(
                Triple("bot", Color.BLUE, PlayerType.STOCKFISHWANNABE),
                Triple("p2", Color.GREEN, PlayerType.PLAYER),
                Triple("p3", Color.YELLOW, PlayerType.PLAYER),
                Triple("p4", Color.RED, PlayerType.PLAYER),
            ),
            gameMode = GameMode.FOUR_PLAYER
        )
    }


    /* /**
     * it tests if the bot chooses the largest block when multiple options are legal and it gives the bot one small
     * block and on large block and verifies that the bot places the large one on the board
     */
    @Test
    fun chooseBestWhenValid(){
        create4PGame()
        val game = rootService.currentGame ?: return
        val state = game.gameStates[game.currentStateIndex]
        val currentPlayer = state.players.first()
        val smallBlock = currentPlayer.playerBlocks.first{it.blockType == BlockType.O1 }
        val bigBlock = currentPlayer.playerBlocks.first{ it.blockType == BlockType.I5 }

        println("Small block")
        println(smallBlock.toString())
        println("Big block")
        println(bigBlock.toString())
        assertEquals(currentPlayer.playerType, PlayerType.STOCKFISHWANNABE,
            "Player should be bot")

        currentPlayer.playerBlocks.clear()
        assertTrue ( currentPlayer.playerBlocks.isEmpty(),"blocks should be cleared")
        currentPlayer.playerBlocks.add(smallBlock)
        assertTrue (currentPlayer.playerBlocks.contains(smallBlock), "Should contain small block")
        currentPlayer.playerBlocks.add(bigBlock)
        assertTrue (currentPlayer.playerBlocks.contains(bigBlock), "Should contain small block")

        botService.chooseBestMove()

        println(game.blokusGameState.gameBoard)
        assertEquals(1, game.currentStateIndex)

        val newState = game.gameStates[game.currentStateIndex]
        val newCurrentPlayer = newState.players.first()
        println("Player blocks")
        currentPlayer.playerBlocks.forEach {
            println("${it.blockType}")
            println(it.toString()) }
        assertFalse(currentPlayer.playerBlocks.contains(bigBlock),
            "Player ${currentPlayer.name} biggest block should be placed")
        assertEquals("p2",newCurrentPlayer.name,
            "bot move has ended so next player is next")

        //assertFalse(newPlayer.playerBlocks.any { it.blockType == BlockType.O1 })
        //assertTrue(newPlayer.playerBlocks.any { it.blockType == BlockType.I5 })

        assertEquals(5, newState.gameBoard.flatten().count { it == Color.RED })
    } */


    /**
     * test that the bot correctly skips it´s turn if it has no blocks left to play and it verifies that the game
     * moves to the next layer and marks the bot´s color as "passed" without crashing
     */
    @Test
    fun passWhenNoNove(){
        create4PGame()
        val game = rootService.currentGame ?: return
        val state = game.gameStates[game.currentStateIndex]
        val currentPlayer = state.players.first()

        currentPlayer.playerBlocks.clear()
        botService.chooseBestMove()

        assertEquals(0, game.currentStateIndex)
        assertEquals("p2", state.players.first().name)
        assertTrue(state.colorHasPassed.last())
        assertTrue(state.players.last().playerBlocks.isEmpty())
        assertEquals(0, state.gameBoard.flatten().count { it == Color.RED })
    }











    /** uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuffffffffffffffffffffffffffffffffffffffff*/









//
//
//
//    /**
//     * tests if the bot is smart enough to chose a big block (5tiles) istade  of a small blicl (1tile) when
//     * bott are legal to place, and then it checks if the game state updates correctly after the move
//     */
//    @Test
//    fun placeLargestLegalBlock(){
//        create4PGame()
//        val game = rootService.currentGame ?: return
//        val currentPlayer = game.gameStates[game.currentStateIndex].players.first()
//        val smallBlock = currentPlayer.playerBlocks.first() {it.blockType == BlockType.O1 }
//        val bigBlock = currentPlayer.playerBlocks.first() {it.blockType == BlockType.I5 }
//
//        currentPlayer.playerBlocks.clear()
//        currentPlayer.playerBlocks.add(smallBlock)
//        currentPlayer.playerBlocks.add(bigBlock)
//        botService.chooseBestMove()
//
//        assertEquals(1, game.currentStateIndex)
//
//        val newState = game.gameStates[game.currentStateIndex]
//        val newPlayer = newState.players.first()
//
//        assertEquals(1, newPlayer.playerBlocks.size)
//        assertTrue(newPlayer.playerBlocks.any() {it.blockType == BlockType.O1})
//        assertFalse(newPlayer.playerBlocks.any() {it.blockType == BlockType.I5})
//        assertEquals(5, newState.gameBoard.flatten().count{it == Color.RED})
//    }
////    fun `chooseBestMove places the largest legal block`() {
////        create4PGame()
////
////        val game = checkNotNull(rootService.currentGame)
////        val currentPlayer = game.gameStates[game.currentStateIndex].players.first()
////        val smallBlock = currentPlayer.playerBlocks.first { it.blockType == BlockType.O1 }
////        val bigBlock = currentPlayer.playerBlocks.first { it.blockType == BlockType.I5 }
////
////        currentPlayer.playerBlocks.clear()
////        currentPlayer.playerBlocks.add(smallBlock)
////        currentPlayer.playerBlocks.add(bigBlock)
////
////        botService.chooseBestMove()
////
////        assertEquals(1, game.currentStateIndex)
////
////        val newState = game.gameStates[game.currentStateIndex]
////        val newPlayer = newState.players.first()
////
////        assertEquals(1, newPlayer.playerBlocks.size)
////        assertTrue(newPlayer.playerBlocks.any { it.blockType == BlockType.O1 })
////        assertFalse(newPlayer.playerBlocks.any { it.blockType == BlockType.I5 })
////
////        val blueTilesOnBoard = newState.gameBoard.flatten().count { it == Color.BLUE }
////        assertEquals(5, blueTilesOnBoard)
////    }
//
//    /**
//     * test that the bot does not ctach or make  amove if it has no blocks left, and it ensure the game state
//     * if no legal moves are possible
//     */
//    @Test
//    fun doNothingWhenNoLegal(){
//        create4PGame()
//        val game = rootService.currentGame ?: return
//        val currentState = game.gameStates[game.currentStateIndex]
//        val currentPlayer = currentState.players.first()
//
//        currentPlayer.playerBlocks.clear()
//        botService.chooseBestMove()
//
//        assertEquals(0, game.currentStateIndex)
//        assertTrue(currentState.players.first().playerBlocks.isEmpty())
//        assertFalse(currentState.colorHasPassed[0])
//    }
//
////    fun `chooseBestMove does nothing when there is no legal move`() {
////        create4PGame()
////
////        val game = checkNotNull(rootService.currentGame)
////        val currentState = game.gameStates[game.currentStateIndex]
////        val currentPlayer = currentState.players.first()
////
////        currentPlayer.playerBlocks.clear()
////
////        botService.chooseBestMove()
////
////        assertEquals(0, game.currentStateIndex)
////        assertTrue(currentState.players.first().playerBlocks.isEmpty())
////        assertFalse(currentState.colorHasPassed[0])
////    }
}
