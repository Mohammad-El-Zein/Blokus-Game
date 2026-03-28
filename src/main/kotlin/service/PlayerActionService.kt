package service

import entity.*

/** a class that handles the actions of the players in the blokus game */
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Rotates the given block by 90° clockwise
     *
     * @param selectedBlock The [Block] to be rotated
     *
     * Prerequisites:
     * - the block exists and belongs to the current player
     * - the game is running
     * - it's the current players turn
     *
     * Post-conditions:
     * - the block has been rotated by 90° clockwise
     *
     * @returns The method does not have a return value (`Unit`)
     *
     * @throws IllegalStateException if the game is not running, or it's not the current players turn
     * @throws IllegalArgumentException if selectedBlock is empty, or does not belong to current player
     *
     * @sample rotate(Block)
     */
    fun rotate(selectedBlock: Block){

        checkNotNull(rootService.currentGame) { "no active game founded"}

        selectedBlock.tiles = transpose(selectedBlock.tiles)
        mirrorTheTiles(selectedBlock.tiles)

        val currentRotation = selectedBlock.rotation
        when (currentRotation) {
            Rotation.NONE -> selectedBlock.rotation = Rotation.RIGHT
            Rotation.RIGHT -> selectedBlock.rotation = Rotation.BOTTOM
            Rotation.BOTTOM -> selectedBlock.rotation = Rotation.LEFT
            Rotation.LEFT -> selectedBlock.rotation = Rotation.NONE
        }
    }

   private fun transpose(matrix: MutableList<MutableList<Color>>): MutableList<MutableList<Color>> {
        val transposed = mutableListOf<MutableList<Color>>()
        for (i in 0 until matrix[0].size){
            val newRow = mutableListOf<Color>()
            for (j in 0 until matrix.size){
                newRow.add(matrix[j][i])
            }
            transposed.add(newRow)
        }
        return transposed
    }

    private fun mirrorTheTiles(matrix: MutableList<MutableList<Color>>){
        for (idx in 0 until matrix.size / 2){
            val size = matrix.size
            val tmp = matrix[idx]
            matrix[idx] = matrix[size - idx - 1]
            matrix[size - idx - 1] = tmp
        }

    }

    /**
     * Mirrors the given block on the y-Axis
     *
     * @param selectedBlock The [Block] to be mirrored
     *
     * Prerequisites:
     * - the block exists and belongs to the current player
     * - the game is running
     * - it's the current players turn
     *
     * Post-conditions:
     * - the block has been mirrored
     *
     * @returns The method does not have a return value (`Unit`)
     *
     * @throws IllegalStateException if the game is not running, or it's not the current players turn
     * @throws IllegalArgumentException if selectedBlock is empty, or does not belong to current player
     *
     * @sample mirror(Block)
     */
    fun mirror(selectedBlock: Block){

        checkNotNull(rootService.currentGame) { "no active game founded"}

        mirrorTheTiles(selectedBlock.tiles)

        selectedBlock.isMirrored = !selectedBlock.isMirrored
        if (selectedBlock.rotation == Rotation.RIGHT){
            selectedBlock.rotation = Rotation.LEFT
        } else {
            if (selectedBlock.rotation == Rotation.LEFT){
                selectedBlock.rotation = Rotation.RIGHT
            }
        }
    }

    /**
     * Places the given block on the given coordinates
     *
     * @param selectedBlock The [Block] to be placed
     * @param x The x-axis coordinate for the block placement
     * @param y The y-axis coordinate for the block placement
     *
     * Prerequisites:
     * - the block exists and belongs to the current player
     * - the game is running
     * - it's the current players turn
     * - the placement of the block is valid
     *
     * Post-conditions:
     * - the block has been placed to the given coordinates
     *
     * @returns The method does not have a return value (`Unit`)
     *
     * @throws IllegalStateException if the game is not running, or it's not the current players turn
     * @throws IllegalArgumentException if selectedBlock is empty, or does not belong to current player
     * @throws IllegalArgumentException if the values of the coordinates are invalid
     *
     * @sample placeBlock(Block, 0, 0)
     */
    fun placeBlock(selectedBlock: Block, x: Int, y: Int){

        val game = checkNotNull(rootService.currentGame) {" no active game founded"}
        val currentState = game.gameStates[game.currentStateIndex]
        val currentPlayer = rootService.gameService.getCurrentPlayer()  // immer first player ist active player


        require(rootService.gameService.validatePlacement(x,y,selectedBlock))//position valid?
        { "the placement is not valid" }

        //wenn spieler undo gemacht hat dann ein neue Zug macht,dann muss alle state > currentStateIndex +1 löschen
        while (game.gameStates.size > game.currentStateIndex +1) {
            game.gameStates.removeLast()
        }

        //spieler neu Zug macht -> neu GameState speichern,deshalb machen wir new state ist copy von currentState
        //copy macht gleiche Liste für 2 methoden das zerstört undo/redo deshalb koppieren selbst die listen
        // jetzt ist so: zb currentState.players:[player1,player2] , newState.players:[player1_copy,player2_copy]
        val newState = currentState.copy(
            players = currentState.players.map { player -> //map geht durch jeden spieler und erstellt neu copy player
                val newPlayer = Player(player.name, player.playerType)
                newPlayer.score = player.score
                newPlayer.playerBlocks = player.playerBlocks.toMutableList()
                newPlayer
            }.toMutableList(),
            colorOrder = currentState.colorOrder.toMutableList()
        )
        newState.sharedColorPlayerIndex = currentState.sharedColorPlayerIndex
        newState.gameBoard = currentState.gameBoard.map{ it.toMutableList() }.toMutableList()
        newState.colorHasPassed = currentState.colorHasPassed.toMutableList()

        val board = newState.gameBoard
        for (i in selectedBlock.tiles.indices){ // i ist spalten im block
            for(j in selectedBlock.tiles[i].indices){ // j ist zeilen im block

                placeTheTiles(selectedBlock, i, j, x, y, board)
            }
        }
        game.gameStates.add(newState)
        game.currentStateIndex++

        newState.players.forEach { player ->
            player.playerBlocks.removeIf {
                it.blockType == selectedBlock.blockType &&
                        it.color == selectedBlock.color
            }
        }




        onAllRefreshables { refreshAfterBlockPlaced() }

        callNetworkLayer(selectedBlock, currentPlayer, x, y)

        rootService.gameService.endTurn()
    }

    private fun callNetworkLayer(selectedBlock: Block, currentPlayer: Player, x: Int, y: Int) {
        // if we are playing an online game, and it is currently our move we send our action to the network
        if (rootService.networkService.client != null
            && rootService.networkService.client?.playerName == currentPlayer.name ) {
            rootService.networkService.sendAction(
                selectedBlock.isMirrored, Pair(x, y), selectedBlock.rotation, selectedBlock.blockType)
        }
    }

    private fun placeTheTiles(selectedBlock: Block, i: Int, j: Int, x: Int, y: Int,
                              board: MutableList<MutableList<Color>>){
        // aus KDoc, in Board x sind spalten, y Zeilen
        if (selectedBlock.tiles[i][j] in setOf( Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW )) {
            val boardForX = x+i //spalten+spalten= aktuelle spalte in Board
            val boardForY = y+j  //zeile+zeile= aktuelle zeile in Board

            if (boardForY >= board.size || boardForX >= board[0].size) {
                throw IllegalArgumentException("Block outside board")
            }
            board[boardForX][boardForY] = selectedBlock.tiles[i][j]
        }
    }

    /**
     * Undo the last played move
     *
     * Prerequisites:
     * - there is at least one played move
     * - the game is running
     * - it's the current players turn
     *
     * Post-conditions:
     * - the last move has been undone
     *
     * @returns The method does not have a return value (`Unit`)
     *
     * @throws IllegalStateException if the game is not running, or it's not the current players turn
     * @throws IllegalStateException if there is no action applied yet
     * @throws IllegalStateException if no game state set yet
     *
     * @sample undo()
     */
    fun undo() {
        val game = checkNotNull( rootService.currentGame)
        if ( game.currentStateIndex <= 0){ //history prüfen
            throw IllegalStateException("No action applied yet.")
        }
        if ( game.gameStates.isEmpty()){
            throw IllegalStateException("No game state set yet.")
        }

        game.currentStateIndex--
        onAllRefreshables {refreshAfterUndo()}
    }

    /**
     * Redo the undone moves until it is a human players turn again
     *
     * Prerequisites:
     * - there is at least one undone move
     * - the game is running
     * - it's the current players turn
     *
     * Post-conditions:
     * - skipped forward so it is a human players turn again
     *
     * @returns The method does not have a return value (`Unit`)
     *
     * @throws IllegalStateException if the game is not running, or it's not the current players turn
     * @throws IllegalStateException if there is no move to redo
     * @throws IllegalStateException if no game state set yet
     *
     *
     * @sample redo()
     */
    fun redo() {
        val game = checkNotNull( rootService.currentGame)
        if ( game.gameStates.isEmpty()){
            throw IllegalStateException("No game state set yet.")
        }
        if (game.currentStateIndex >= game.gameStates.size -1 ){//undo moves back, redo moves forward so currentStateIdx
            throw IllegalStateException("No move to redo.") // has to be smaller than size of the list
        }

        game.currentStateIndex++
        onAllRefreshables {refreshAfterRedo()}
    }
}