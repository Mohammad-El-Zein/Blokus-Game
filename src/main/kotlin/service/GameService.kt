package service

import entity.*
import kotlin.collections.indices
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File


/** a class that handles the internal functions of the blokus game */
class GameService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Creates a game with chosen gameMode, players and the colorOrder
     *
     * @param gameMode The chosen gameMode
     * @param players The List of the players
     * @param isAdvancedScoring Decision of whether using advanced scoring or not.
     *
     * Prerequisites:
     * - there is no active game
     *
     * Post-conditions:
     * - the game is running
     * - the players were created
     * - the blocks were created
     * - the players have blocks
     * - one player is chosen to play first
     *
     * @returns The method does not have a return value (`Unit`)
     *
     * @throws IllegalStateException if there is a game already running
     * @throws IllegalStateException if [players] does not have the correct amount of players for the [gameMode],
     * @throws IllegalStateException if [players] do not match the color requirements for [gameMode]
     *
     */
    fun createGame(isAdvancedScoring : Boolean , players: List<Triple<String,Color,PlayerType>>, gameMode : GameMode) {
        check ( rootService.currentGame == null) { "Game already exists."}

        //manuell  Liste nur mit Spieler erstellen
        // Distinct Idee ( für den Case TWOPLFOURCOL: Erstmal nur Spielernamen mit ersten zugewiesenen Color.
        // Später bei createPieces wird
        // eh index+2 genommen dort bekommt der Spieler von colorOrder seine zweite Farbe.
        // so kann amountPlayer bei TwoPlFourCol 2 bleiben
        // ergänzung 19.03.26 : it.third jetzt kann der PlayerType festgelegt werden
        val takePlayers = players.distinctBy{it.first}.map { Player( it.first,it.third)}.
                toMutableList()



        // Consider amount of players in each gameMode
        val amountPlayer = when(gameMode) {
            GameMode.FOUR_PLAYER -> 4
            GameMode.THREE_PLAYER -> 3
            GameMode.TWO_PLAYER_SMALL -> 2
            GameMode.TWO_PLAYER_FOUR_COLOR -> 2
        }
        if ( takePlayers.size != amountPlayer ) {
            throw IllegalArgumentException("Invalid number of players has been chosen.")
        }


        // In case of three players, one color must be shared otherwise the color is blank
        // in our case the last color of the list will be shared
        val colorRule = if (gameMode == GameMode.THREE_PLAYER) {
            val playerColors = players.map { it.second }.toSet()
            listOf(Color.BLUE, Color.YELLOW, Color.RED, Color.GREEN).first { it !in playerColors }
        } else {
            Color.BLANK
        }


        val colorOrder = when (gameMode) {
            GameMode.TWO_PLAYER_FOUR_COLOR, GameMode.THREE_PLAYER ->
                mutableListOf(Color.BLUE, Color.YELLOW, Color.RED, Color.GREEN)
            else -> players.map { it.second }.toMutableList()
        }


        // with these conditions and rules, we create a blokusgamestate instance which contains all information about
        //the game
        val gameState = BlokusGameState(
            players = takePlayers,
            gameMode = gameMode,
            advancedScoring = isAdvancedScoring,
            sharedColor = colorRule,
            colorOrder = colorOrder
        )



        // for Each column entry of mutable list create a mutable list ( row wise) with all colors blank
        gameState.gameBoard = createGameBoard(gameMode)

        gameState.colorHasPassed = MutableList(colorOrder.size) { false }


        // create BlokusGame with its state instance
        val game = BlokusGame(gameState)


        // start the game and create all play pieces
        rootService.currentGame = game
        createPieces()

        onAllRefreshables { refreshAfterCreateGame() }
        rootService.botService.calculateTurn()
    }

    /**
     * Calculates the score of every player according to the scoring rules
     *
     * Prerequisites:
     * - the game is over
     *
     * Post-conditions:
     * - every player has a calculated score
     * - the gui is presenting the scores
     *
     * @returns The method does not have a return value (`Unit`)
     *
     * @throws IllegalStateException if the game is still running
     *
     * @sample calculateScore()
     */
    fun calculateScore() {
        check(checkForGameEnd()) { "The game is still running" }
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]

        // get a list of colors to match the player list by index
        // copy of colorOrder list
        val playerColorOrder = state.colorOrder.toMutableList()
        // remove sharedColor from playerColorOrder list
        playerColorOrder.remove(state.sharedColor)
        // Is shared color the first element in colorOrder?
        if (state.sharedColor == state.colorOrder.first()) {
            // rotate the color list back to match the player list
            playerColorOrder.add(0, playerColorOrder.removeLast())
        }

        // Calculate score for each player
        for ((playerIndex, player) in state.players.withIndex()) {
            var score = 0
            // count the number of squares in the remaining blocks
            for (block in player.playerBlocks) {
                score += getValueOfBlock(block)
            }

            if (state.advancedScoring) {
                // make score negative
                score = - score
                // get colors of the player
                val colors = mutableListOf(playerColorOrder[playerIndex])
                if (state.gameMode == GameMode.TWO_PLAYER_FOUR_COLOR) {
                    colors.add(playerColorOrder[playerIndex + 2])
                }
                // add bonus points
                score += calculateBonusPoints(player, colors)
            }
            // save the score of the player
            player.score = score
        }
    }

    private fun getValueOfBlock(block: Block): Int {
        when (block.blockType) {
            BlockType.I5,
            BlockType.N5,
            BlockType.V5,
            BlockType.T5,
            BlockType.U5,
            BlockType.L5,
            BlockType.Y5,
            BlockType.Z5,
            BlockType.W5,
            BlockType.P5,
            BlockType.X5,
            BlockType.F5
                -> return 5
            BlockType.Z4,
            BlockType.I4,
            BlockType.L4,
            BlockType.O4,
            BlockType.T4
                -> return 4
            BlockType.I3,
            BlockType.V3
                -> return 3
            BlockType.I2
                -> return 2
            BlockType.O1
                -> return 1
        }
    }

    /**
     * calculate the bonus points for the given player with the given colors
     *
     * @param player The player whose points are being calculated.
     * @param colors The list of colors belonging to the player.
     */
    private fun calculateBonusPoints(player: Player, colors: List<Color>): Int {
        check(checkForGameEnd()) { "The game is still running" }

        // initialize score to return
        var score = 0
        // calculate scoring for each color
        for (color in colors) {
            // +15 points if all blocks with the color have been placed
            if (player.playerBlocks.none { block -> block.color == color }) {
                score += 15

                // +5 points if the last piece with the color was the smallest piece
                // Is last block of the color the O1 block?
                if (findLastBlockOfColor(player, color).blockType == BlockType.O1) {
                    score += 5
                }

            }
        }
        return score
    }

    private fun findLastBlockOfColor(player: Player, color: Color): Block {
        val game = checkNotNull(rootService.currentGame)
        // Search for last state, where the player has one block of the color
        for (stateIndex in game.currentStateIndex downTo 0) {
            // Get the matching state
            val oldState = game.gameStates[stateIndex]
            // find the player to calculate score
            val oldPlayer = oldState.players.first { oldPlayer -> oldPlayer.name == player.name }
            // Get all blocks of the player with the color
            val blocksByColor = oldPlayer.playerBlocks.filter { block -> block.color == color }
            // only one block of the color left
            if (blocksByColor.size == 1) {
                return blocksByColor[0]
            }
        }
        // not possible for a correct call of this function
        return Block(Color.BLOCKED, BlockType.I5)
    }

    /**
     * Create all pieces for the players
     *
     * Prerequisites:
     * - there are players
     *
     * Post-conditions:
     * - every player has exactly 21 pieces
     *
     * @returns The method does not have a return value (`Unit`)
     *
     * @throws IllegalStateException if pieces already exist
     *
     * @sample createPieces()
     */
    private fun createPieces() {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]
        var sharedColorIsPasses = 0
        for ( (index,player) in state.players.withIndex()) {
            //index is needed for assigning the color to the player
            //colorOrder and players are two different lists therefore we need a common access
            if ( player.playerBlocks.isNotEmpty()){
                throw IllegalStateException("pieces already exist")
            }
            if(state.colorOrder[index] == state.sharedColor){
                sharedColorIsPasses = 1
            }
            val colorAssign = state.colorOrder[index+sharedColorIsPasses]

            for ( blocks in BlockType.entries){
                player.playerBlocks.add( Block(colorAssign,blocks))
            }
            // the case of TWO PLAYER FOUR COLOR : Add colorOrder[index+2] for each player , not index+1 because it is
            //already given to player2 ( the first color of player 2 )
            if ( state.gameMode == GameMode.TWO_PLAYER_FOUR_COLOR){
                val secondColorAssign = state.colorOrder[index+2]
                for ( blocks in BlockType.entries){
                    player.playerBlocks.add( Block(secondColorAssign,blocks))
                }
            }
            if (state.gameMode == GameMode.THREE_PLAYER) {
                for (blocks in BlockType.entries) {
                    player.playerBlocks.add(Block(state.sharedColor, blocks))
                }
            }

        }

    }

    /**
     * Determine the next player based on gameMode, if the current player move is done.
     * For the three player game, the player for the last color is calculated by (gameRound-1)%3
     *
     * Prerequisites:
     * - the game is running
     *
     * Post-conditions:
     * - the list colorOrder is shifted by one
     * - the list players is shifted by one,
     *   unless it is a three players game, in which case the greenPlayerIdx is increased
     *
     * @returns The method does not have a return value (`Unit`)
     *
     * @throws IllegalStateException if there is no game active
     *
     * @sample nextPlayer()
     */
     fun nextPlayer() {

        val game = checkNotNull(rootService.currentGame) { "no active game found" }

        val state = game.gameStates[game.currentStateIndex]


        when (state.gameMode) {
            GameMode.THREE_PLAYER -> {
                //The players and sharedColorPlayerIndex are shifted in sync
                //If the last turn was the sharedColor (meaning colorOrder[0] is sharedColor)
                //the players are not shifted and the index is shifted in the other direction
                if (state.colorOrder.first() != state.sharedColor) {
                    val player1 = state.players.removeFirst()
                    state.players.add(player1)

                    state.sharedColorPlayerIndex--
                    if(state.sharedColorPlayerIndex < 0) {
                        state.sharedColorPlayerIndex = state.players.size-1
                    }
                } else {
                    state.sharedColorPlayerIndex++
                    if(state.sharedColorPlayerIndex >= state.players.size) {
                        state.sharedColorPlayerIndex = 0
                    }
                }

                println(state.sharedColorPlayerIndex)
                val color1 = state.colorOrder.removeFirst()
                val passed1 = state.colorHasPassed.removeFirst()

                state.colorOrder.add(color1)
                state.colorHasPassed.add(passed1)

                rootService.botService.calculateTurn()


            }

            else -> {//im Fall: "FOUR_PLAYER, TWO_PLAYER_SMALL, TWO_PLAYER_FOUR_COLOR"
                val color1 = state.colorOrder.removeFirst()
                val player1 = state.players.removeFirst()
                val passed1 = state.colorHasPassed.removeFirst()



                //  color und player wird removed von anfang, added am ende
                state.colorOrder.add(color1)
                state.players.add(player1)
                state.colorHasPassed.add(passed1)
                rootService.botService.calculateTurn()
            }


        }


    }

    /**
     * Saves the current game state
     *
     * @param gameName The name of the saved game
     *
     * Prerequisites:
     * - the game is running
     * - the game is an offline game
     *
     * Post-conditions:
     * - the current game is saved
     *
     * @returns The method does not have a return value (`Unit`)
     *
     * @throws IllegalStateException if there is no active game, or the game is an online game
     *
     * @sample saveGame()
     */
    fun saveGame(gameName: String) {
        val game = checkNotNull(rootService.currentGame) { "no active game found" }

        // serialize the game (game as a string)
        val gameAsString = Json.encodeToString(game)
        // create .json file
        val file = File("$gameName.json")
        // write the game in the file
        file.writeText(gameAsString)

        onAllRefreshables { refreshAfterSaveGame() }
    }

    /**
     * Loads the selected saved game
     *
     * @param savedGame The path to the saved game
     *
     * Prerequisites:
     * - there is no active game8
     * - there is at least one saved game
     *
     * Post-conditions:
     * - the selected game is running
     *
     * @returns The method does not have a return value (`Unit`)
     *
     * @throws IllegalStateException if there is no selected saved game
     *
     * @sample loadGame(*pathToFile*)
     */
    fun loadGame(savedGame: String) {
        // load savedGame file
        val file = File("$savedGame.json")
        check(file.exists()) { "The game file doesn't exist" }

        // read the file
        // decode the file content to a BlokusGame object
        val gameAsString = file.readText()
        rootService.currentGame = Json.decodeFromString<BlokusGame>(gameAsString)

        onAllRefreshables { refreshAfterLoadGame() }
    }

    /**
     *  Checks whether all players have passed their move and therefore game can be ended
     *
     *  Prerequisites:
     *  - game should be running
     *
     *  @return `true` if all players have passed, `false` otherwise
     *  @throws IllegalStateException if there is no selected active game.
     *  @sample checkForGameEnd
     */
    private fun checkForGameEnd() : Boolean {
        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]
         return if (state.gameMode!= GameMode.THREE_PLAYER){
              state.colorHasPassed.all { it } // colorHasPassed true, wenn nichts mehr gespielt werden kann
         }
        else {
            // ziel : suche nach dem selben index wo shared color in der colorOrder List platziert ist und
            // ignoriere sharedColor, weil wenn alle 3 Spieler nicht mehr mit ihren Farben spielen können, kann shared
            // Color sowieso nicht mehr spielen
            val sharedColorIndex = state.colorOrder.indexOf(state.sharedColor)
             state.colorHasPassed.filterIndexed { index, _ -> // index wichtig
                 index != sharedColorIndex
             }.all {it} // selbe logik wie für alle anderen Spielmodi, nur ohne sharedColor , alle müssen passen

         }


    }

    /**
     * Returns the current player of the game
     *
     * Prerequisites:
     * - the game is running
     *
     * Post-conditions:
     * - no game state has been changed
     *
     * @returns The current [Player] whose turn it is
     *
     * @throws IllegalStateException if the game is not running
     *
     * @sample getCurrentPlayer()
     */
    fun getCurrentPlayer(): Player {
        val game = checkNotNull(rootService.currentGame) { "no active game found" }
        val state = game.gameStates[game.currentStateIndex]

        // in modus THREE_PLAYER,wenn sharedColor dran ist brauchen getCurrentPlayer nicht da haben sharedColorPlayeridx
        return if (state.gameMode == GameMode.THREE_PLAYER &&
            state.colorOrder.first() == state.sharedColor) {
            state.players[state.sharedColorPlayerIndex]// sharedColorPlayerIndex zeigt welcher Spieler jetzt spielt
        } else {
            // alle anderen game Modus soll erster Spieler immer aktuell
            state.players.first()
        }
    }

        /**
     * Ends the current player's turn and moves to the next player
     *
     * Prerequisites:
     * - the game is running
     * - it's the current players turn
     *
     * Post-conditions:
     * - the current players turn has ended
     * - the next player is determined
     * - the game is checked for an end condition
     *
     * @returns The method does not have a return value (`Unit`)
     *
     * @throws IllegalStateException if the game is not running, or it's not the current players turn
     *
     * @sample endTurn()
     */
    fun endTurn() {
        val game = checkNotNull(rootService.currentGame) {"no active game found"}
        val state = game.gameStates[game.currentStateIndex]

        if (!checkForPossibleMove()) { // ist gleich auf(checkForPossibleMove() == false)
            state.colorHasPassed[0] = true //also wenn keine mögliche move noch hat dann farbe passed -> nächste spieler
        }

        //nextPlayer()

        if(checkForGameEnd()) { // gleich auf checkForGameEnd() == true
            calculateScore()
            onAllRefreshables { refreshAfterGameEnd() }
            return
        }

        nextPlayer()
        
        while (!checkForPossibleMove()) {
            val currentState = game.gameStates[game.currentStateIndex]
            currentState.colorHasPassed[0] = true
            nextPlayer()

            if (checkForGameEnd()) {
                calculateScore()
                onAllRefreshables { refreshAfterGameEnd() }
                return
            }
        }

        onAllRefreshables { refreshAfterEndTurn() }
    }

    /**
     * Validates if the placement of the given block on the given coordinates is valid
     * @param xCoord The x-axis coordinate for the block placement
     * @param yCoord The y-axis coordinate for the block placement
     * @param block The [Block] to be placed
     *
     * Prerequisites:
     * - the game is running
     * - the block exists and belongs to the current player
     *
     * Post-conditions:
     * - no game state has been changed
     *
     * @returns `true` if the placement is valid, `false` otherwise
     *
     * @throws IllegalStateException if the game is not running
     * @throws IllegalArgumentException if the block does not belong to the current player,
     * or the coordinates are out of bounds
     *
     * @sample validatePlacement(1, 0, Block)
     */
    fun validatePlacement(xCoord: Int, yCoord: Int, block: Block): Boolean {
        val game = checkNotNull(rootService.currentGame) {"no active game found"}
        val state = game.gameStates[game.currentStateIndex]
        // Check if x and y coordinates are in of bounds
        require(xCoord in 0..state.gameBoard.size - 3) {
            "x-coordinate is out of bounds"
        }
        require(yCoord in 0..state.gameBoard.size - 3) {
            "y-coordinate is out of bounds"
        }

        val isFirstMove = (game.currentStateIndex < 4 && state.gameMode != GameMode.TWO_PLAYER_SMALL) ||
                game.currentStateIndex < 2

        var touchesDiagonal = false
        // Iterate over all tiles of the block
        for (i in block.tiles.indices) { // "i" is the row in the block
            for (j in block.tiles[i].indices) { // j is the column in the block
                val boardForX = xCoord + i // column + column = current column in the block
                val boardForY = yCoord + j  // row + row = current row in the block
                if (boardForY >= state.gameBoard.size || boardForX >= state.gameBoard.size) {
                    return false
                }
                // The color in the gameboard
                val gameBoardColor = state.gameBoard[boardForX][boardForY]
                // The color of the tile
                val tileColor = block.tiles[i][j]

                // The gameboard color is not blank --> can not place the tile
                if (tileColor in setOf( Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW ) &&
                    gameBoardColor != Color.BLANK) {
                    return false
                }
                // The color of the game board is the same as the color of the block.
                else if (tileColor == Color.ADJACENT && gameBoardColor == block.color) {
                    return false
                }
                // the block touches diagonal to a corner (fist move) or same color
                else if (tileColor == Color.DIAGONAL &&
                    checkTouchesDiagonal(isFirstMove, gameBoardColor, block.color)) {
                        touchesDiagonal = true
                }
            }
        }
        return touchesDiagonal
    }

    private fun checkTouchesDiagonal(isFirstMove: Boolean, gameBoardColor: Color, blockColor: Color): Boolean {
        if (isFirstMove) {
            // The block touches one corner of the game board
            if (gameBoardColor == Color.CORNER) {
                return true
            }
        }
        // The block touches a block of the same color diagonally.
        else if (gameBoardColor == blockColor) {
            return true
        }
        return false
    }

    /**
     * Checks if the current player has at least one possible move
     *
     * Prerequisites:
     * - the game is running
     * - it's the current players turn
     *
     * Post-conditions:
     * - no game state has been changed
     *
     * @returns `true` if the current player has at least one possible move, `false` otherwise
     *
     * @throws IllegalStateException if the game is not running
     *
     * @sample checkForPossibleMove()
     */
    private fun checkForPossibleMove(): Boolean {
        val game = checkNotNull(rootService.currentGame) { "no active game found" }
        val state = game.gameStates[game.currentStateIndex]
        val currentPlayer = rootService.gameService.getCurrentPlayer()
        val boardSize = state.gameBoard.size

        /* if you don't understand what is going on, check out the state-machine in our group chat */
        /* the method moves through the 8 unique states as follows( isMirrored/rotation ):
        * 0/0 -> 0/1 -> 0/2 -> 0/3 -> 1/1 -> 1/2 -> 1/3 -> 1/0 */
        for (block in currentPlayer.playerBlocks) {
            if (block.color == state.colorOrder[0]) {
                // use a copy of the original to not accidentally mess up the blocks
                val tempBlock = Block(block.color, block.blockType)
                // check all possible rotations when not mirrored
                if (checkTheRotations(tempBlock, boardSize)) return true
                // check all possible rotations when mirrored
                rootService.playerActionService.mirror(tempBlock)
                if (checkTheRotations(tempBlock, boardSize)) return true
            }
        }
        return false
    }

    private fun checkTheRotations(block: Block, boardSize: Int): Boolean {
        if (checkForValidPlacement(block, boardSize)) return true   // check initial rotation
        rootService.playerActionService.rotate(block)
        if (checkForValidPlacement(block, boardSize)) return true   // check initial rotation + 1
        rootService.playerActionService.rotate(block)
        if (checkForValidPlacement(block, boardSize)) return true   // check initial rotation + 2
        rootService.playerActionService.rotate(block)
        return checkForValidPlacement(block, boardSize)             // check initial rotation + 3
    }

    private fun checkForValidPlacement(block: Block, boardSize: Int): Boolean {
        for (x in 0..boardSize - 3) {
            for (y in 0..boardSize - 3) {
                if (validatePlacement(x, y, block)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     *  Hilfsfunktion :
     *  Generates the game board for the given game mode.
     *  @return 2D MutableList of [Color].
     *  @param gameMode Decides about 16x16 or 22x22
     */

    private fun createGameBoard(gameMode : GameMode) : MutableList<MutableList<Color>> {
        // 16x16 on TWO PLAYER SMALL and 22x22 otherwise ( extra row and column on each side so every field has a
        // neighbor and validatePlacement does not get an index out of bounds exception)
        //  -> validatePlacement does not check neighbors on CORNER and BLOCKED therefore we do not get an exception
        val boardSize = if (gameMode == GameMode.TWO_PLAYER_SMALL) {
            16
        } else {
            22
        }
        val board = MutableList(boardSize) { MutableList(boardSize) { Color.BLANK } } // creating board[][]

        // at first, state BLOCKED for every field on the edge
        for ( i in 0 until boardSize) {
            board[i][0] = Color.BLOCKED
            board[i][boardSize-1] = Color.BLOCKED
            board[0][i] = Color.BLOCKED
            board[boardSize-1][i] = Color.BLOCKED
        }

        // next , all states of the fields in the corners are set so CORNER
        board[0][0] = Color.CORNER
        board[0][boardSize-1] = Color.CORNER
        board[boardSize-1][0] = Color.CORNER
        board[boardSize-1][boardSize-1] = Color.CORNER


        return board // continue in createGame
    }
}



