package service.bot

import entity.Block
import entity.Color
import entity.PlayerType
import entity.Rotation
import service.RootService
import service.bot.BotConstants.MAX_THINKING_TIME_MS
import kotlin.math.exp

/**
 * this class is responsible for controlling the bot in the blokus game. and its
 * main job is to look at the current game board and decide which move is the best for the "pro bot"
 * to make during its turn, or take a random move for the "test bot"
 *
 * @property rootService the main service that connects the bot to the rest of the game logic,
 * such as the board state and player actions
 * @property botDelay: LONG defines the wait time until the bot makes an action
 */
class BotService(private  val rootService: RootService,) {
    var botDelay = 700L
    /**
     * [calculateTurn] Safely chooses the bot's behavior according to it's
     * PlayerType if player isn't bot it returns to the method who called
     * [calculateTurn].
     */
    fun calculateTurn() {

         checkNotNull(rootService.currentGame) { "Game is null" }
        val currentPlayer = rootService.gameService.getCurrentPlayer()

            // Create a background thread so the UI doesn't freeze
            Thread {
                // Add a 0,7-second delay (700ms)
                Thread.sleep(botDelay)

                // Now run the bot logic
                when (currentPlayer.playerType) {
                    PlayerType.STOCKFISHWANNABE -> chooseBestMove()
                    PlayerType.TESTBOT -> chooseRandomMove()
                    else -> return@Thread
                }
            }.start()
            /**
             * should be used in nextPlayer fun, witch give the bot if his turn the next the chance to
             * be aware when the game stat and choose the stratige that hw eill use (noop bot or pro one)
             */
    }


    /**
     *[calculatePossibleMoves] this function finds all possible moves for the bot
     * @return MutableList<Triple<Block, Pair<Int, Int>, Pair<Rotation, Boolean>>>
     * which is the list with possible moves and block
     */
    fun calculatePossibleMoves(deadline : Long): MutableList<Triple<Block, Pair<Int, Int>, Pair<Rotation, Boolean>>> {

        val game = rootService.currentGame ?: return mutableListOf()
        val currentState = game.gameStates[game.currentStateIndex]
        val currentPlayer = rootService.gameService.getCurrentPlayer()
        val possibleMoves = mutableListOf<Triple<Block, Pair<Int, Int>, Pair<Rotation, Boolean>>>()
        val deadline = thinkingDeadLine()

        val boardSize = game.blokusGameState.gameBoard.size - 2

        for (block in currentPlayer.playerBlocks) {
            if(block.color == currentState.colorOrder[0]) {
                for (rotation in Rotation.entries) {
                    for (isMirrored in listOf(false, true)) {
                        if (System.currentTimeMillis() >= deadline) return possibleMoves

                        val transformedBlock = createTransformedBlock(block, rotation, isMirrored)


                        for (x in 0 until boardSize) {
                            for (y in 0 until boardSize) {
                                if (System.currentTimeMillis() >= deadline) return possibleMoves
                                if (rootService.gameService.validatePlacement(x, y, transformedBlock)) {
                                    possibleMoves.add(Triple(block, Pair(x, y), Pair(rotation, isMirrored)))
                                }
                            }
                        }
                    }
                }
            }
        }
            return possibleMoves
    }

    private fun thinkingDeadLine(): Long = System.currentTimeMillis() + BotConstants.MAX_THINKING_TIME_MS

    /**
     *[chooseRandomMove] calls [calculatePossibleMoves] to get the list of all possible moves
     *it chooses one of those moves and calls [makeMove] with the random move as parameter
     */
    private fun chooseRandomMove(){
        val deadline = thinkingDeadLine()
        val moves = calculatePossibleMoves(deadline)

         if ( moves.isNotEmpty()) {
             val randomMove = moves.random()
             makeMove(randomMove)
         }
    }

    /**
     * [chooseBestMove]
     * choose the best move from those found in the calculatePossibleMoves function according to greedy algo,
     * witch means, putting the large blocks in the first and rid of them, and this belongs to the "pro bot"
     */
     fun chooseBestMove() {

        val game = rootService.currentGame ?: return
        val currentState = game.gameStates[game.currentStateIndex]
        val deadline = thinkingDeadLine()
        val moves = calculatePossibleMoves(deadline)

        if ( moves.isEmpty() ) {
            currentState.colorHasPassed[0] = true
            rootService.gameService.endTurn()
            return
        }
        var bestMove = moves.maxByOrNull { move ->
            evaluateMove(move)
        }
        var bestScore = Int.MIN_VALUE

        for (move in moves) {
            if (System.currentTimeMillis() >= deadline) {
                break
            }
            val score = evaluateMove(move)
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }

        if (bestMove != null) {
            println("Best move is ${bestMove.first.blockType} \n${bestMove.first}")
            makeMove(bestMove)
        }
    }

    /**
     *[evaluateMove]
     * Using a heuristic approach of attributing points to the bot choices:
     * - Biggest block goes first
     * - Center control: Gives access to more directions
     * - Maximize future corners
     * - Prefer usable corners -> if there is enough size for the rest of the tiles
     * - Places the piece where surrounding area is open
     * - selfblocking penalty
     * - Difference between early-game and late-game
     *      Early game: big piece,center, expansion
     *      late-game: future mobility, fit, usable corners
     *@return Int that corresponds as the value of the
     */
    private fun evaluateMove(move: Triple<Block, Pair<Int, Int>, Pair<Rotation, Boolean>>): Int  {
        val game = rootService.currentGame ?: return 0
        val currentStat = game.gameStates[game.currentStateIndex]
        val block = move.first
        val x = move.second.first
        val y = move.second.second
        val rotation = move.third.first
        val isMirrored = move.third.second

        val transformedBlock = createTransformedBlock(block, rotation, isMirrored)

        /** calculate how close the move is to the center of the board (coz staying near to the center prevents the
         * bot from being trapped in the corner)
         */
        // Gives bonus points if block has bigger pieces
        val blockSize = transformedBlock.tiles.flatten().count{it == transformedBlock.color}
        val boardSize = currentStat.gameBoard.size
        val center = boardSize / 2
        val distanceToCenter = kotlin.math.abs(x - center) + kotlin.math.abs(y - center)
        val centerScore = boardSize - distanceToCenter
        val expansionChance = transformedBlock.tiles.flatten().count{it == Color.DIAGONAL}
        /** to count tils marked DIAGONAL*/

        /**
         * this check if the new corners after the placment are in the board size and can be used (to make more optins)
         */
        var futureMoveChance = 0
        transformedBlock.tiles.indices.forEach { i ->
            transformedBlock.tiles[i].indices.forEach { j ->
                if (transformedBlock.tiles[i][j] == Color.DIAGONAL) {
                    val boardX = x + j
                    val boardY = y + i
                    if (
                        boardY in currentStat.gameBoard.indices &&
                        boardX in currentStat.gameBoard[boardY].indices &&
                        currentStat.gameBoard[boardX][boardY] == Color.BLANK
                    ) {
                        futureMoveChance++
                    }
                }
            }
        }
        /**
         * Changes here can be decided later
         * score depending on the game phase because:
         * Early game Rewards: big pieces, center, expansion
         * Late game rewards: future legal opportunities, not trapping,
         * efficient fitting
         */
        val remainingBlocks = currentStat.players.first().playerBlocks.size
        val gameBoard = currentStat.gameBoard
        val opponentPressure = countOpponentPressure()
        val score = if (remainingBlocks > 10){ // early game
            blockSize *12 + centerScore*3 + expansionChance *4+ opponentPressure*2+
                    futureMoveChance*2 - selfBlockingPenalty(transformedBlock,x,y,gameBoard) *2
        }
        else{ // LATE GAME PART
            blockSize*6+centerScore*1+
                    expansionChance *3 + futureMoveChance*8 + opponentPressure+
                    - selfBlockingPenalty(transformedBlock,x,y,gameBoard) *5
        }
        return score

        //return blockSize * 10 + centerScore * 2 + expansionChance * 4 + futureMoveChance * 5
    }

    /**
     * Does this move traps future moves?
     */
    private fun selfBlockingPenalty(block: Block,startX: Int,startY: Int,
                                    gameBoard: MutableList<MutableList<Color>>): Int{
        var penalty = 0
        val directions = listOf(Pair(-1,0),Pair(1,0), Pair(0,-1), Pair(0,1))
        // Look at orthogonal neighbors and if they are out of bounds (
        block.tiles.indices.forEach { i->
            block.tiles[i].indices.forEach { j->
                if (block.tiles[i][j] != block.color) return@forEach
                val boardX = startX + i;val boardY = startY + j
                directions.forEach { (dx,dy)->
                    val nx = boardX + dx;val ny = boardY + dy
                    if (ny !in gameBoard.indices || nx !in gameBoard[ny].indices){
                        penalty++
                    }else if (gameBoard[ny][nx] != Color.BLANK){ // Touching not blank
                        penalty++ //adds penalty It's blank because other player pieces also block
                    }   // next moves 
                }
            }
        }
        return penalty
    }

    /**
     * Using opponent pressure as score weights
     * if opponent has many big pieces open space is more important
     * so bot should care more about blocking/expansion
     * how much bot should worry about opponent taking space
     */
    private fun countOpponentPressure(): Int{
        val game = rootService.currentGame ?: return 0
        val currentState = game.gameStates[game.currentStateIndex]
        var pressure=0
        for (player in currentState.players.drop(1)) { // Ignores first player
            for (block in player.playerBlocks) {
                // Counts how many squares a piece contains
                val size = block.tiles.flatten().count { it == block.color }
                pressure+=when (size){  // Larger pieces -> Larger pressure
                    5 -> 3 // Adds blocking value
                    4 -> 2
                    3 -> 1
                    else -> 0
                }
            }
        }

        return pressure
    }


    /**
     * executing the move on the board and updating the roles
     */
    fun makeMove(bestMove: Triple<Block, Pair<Int, Int>, Pair<Rotation, Boolean>>) {
        val game = rootService.currentGame ?: return
        val currentState = game.gameStates[game.currentStateIndex]
        val chosenX = bestMove.second.first
        val chosenY = bestMove.second.second
        val rotation = bestMove.third.first
        val isMirrored = bestMove.third.second
        val originalBlock = bestMove.first

        val transformedBlock = createTransformedBlock(originalBlock, rotation, isMirrored)

        if (rootService.gameService.validatePlacement(chosenX,chosenY, transformedBlock)) {
            originalBlock.tiles = transformedBlock.tiles.map { it.toMutableList() }.toMutableList()
            originalBlock.rotation = transformedBlock.rotation
            originalBlock.isMirrored = transformedBlock.isMirrored
            rootService.playerActionService.placeBlock(originalBlock, chosenX, chosenY)
        } else {
            currentState.colorHasPassed[0] = true
            rootService.gameService.endTurn() /** maybe changing it to nextPlayer*/
        }
    }

    private fun createTransformedBlock(
        block: Block,
        rotation: Rotation,
        isMirrored: Boolean
    ): Block{
        val transformedBlock = Block(block.color, block.blockType)
        transformedBlock.tiles = block.tiles.map {it.toMutableList() }.toMutableList()
        transformedBlock.rotation = Rotation.NONE
        transformedBlock.isMirrored = false

        if (isMirrored) {
            rootService.playerActionService.mirror(transformedBlock)
        }
        repeat(when(rotation){
            Rotation.NONE -> 0
            Rotation.RIGHT -> 1
            Rotation.BOTTOM -> 2
            Rotation.LEFT -> 3
        }){
            rootService.playerActionService.rotate(transformedBlock)
        }
        return transformedBlock
    }
}