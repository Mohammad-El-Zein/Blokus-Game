package gui

import entity.BlockType
import entity.Color
import entity.Rotation
import service.RootService
import tools.aqua.bgw.event.KeyCode
import tools.aqua.bgw.visual.ImageVisual

/**
 * A helper function for the [BlokusGameScene].
 */
class GameSceneComponents(val rootService: RootService, val menuScene: BlokusGameScene) {
    val yellowTileImage = ImageVisual("YellowTile.png")
    val redTileImage = ImageVisual("RedTile.png")
    val blueTileImage = ImageVisual("BlueTile.png")
    val greenTileImage = ImageVisual("GreenTile.png")
    val emptyTileImage = ImageVisual("EmptyTile.png")
    /**
     * is 0 for show Main Blocks, 1 for show secondary Blocks and 2 for hide
     */
    var mainBlocks = 0
    /**
     * Updates the undo and redo button
     */
    fun updateButtons() {
        val game = checkNotNull( rootService.currentGame)
        menuScene.undoButton.isDisabled = game.currentStateIndex <= 0
        menuScene.redoButton.isDisabled = game.currentStateIndex >= game.gameStates.size -1
    }

    /**
     * creates the empty drawBoard for the current boardSize filled with empty tiles
     */
    fun createDrawBoard(){
        val gameHistory = checkNotNull(rootService.currentGame)
        val game = gameHistory.gameStates[gameHistory.currentStateIndex]
        for (i in 0..< game.gameBoard.size-2){
            for (j in 0..< game.gameBoard[i].size-2){
                menuScene.gameBoard[i, j] = BlokusTile(width = menuScene.blockSideLength,
                    height = menuScene.blockSideLength,
                    color = game.gameBoard[i+1][j+1],
                    visual = when(game.gameBoard[i+1][j+1]){
                        Color.YELLOW -> yellowTileImage.copy()
                        Color.RED -> redTileImage.copy()
                        Color.BLUE -> blueTileImage.copy()
                        Color.GREEN -> greenTileImage.copy()
                        else -> emptyTileImage.copy()
                    }
                )
            }
        }
    }

    /**
     * Updates the [entity.BlokusGameState.gameBoard] with the current contents
     */
    fun drawGameBoard(){
        //(game und gamehistory Sachen auch überall so wie hier erstetzen)
        val gameHistory = checkNotNull(rootService.currentGame)
        val game = gameHistory.gameStates[gameHistory.currentStateIndex]
        for (i in 0..< game.gameBoard.size-2){
            for (j in 0..< game.gameBoard[i].size-2){
                menuScene.gameBoard[i, j]?.color =   game.gameBoard[i+1][j+1]
                menuScene.gameBoard[i, j]?.visual = when(game.gameBoard[i+1][j+1]){
                    Color.YELLOW -> yellowTileImage.copy()
                    Color.RED -> redTileImage.copy()
                    Color.BLUE -> blueTileImage.copy()
                    Color.GREEN -> greenTileImage.copy()
                    else -> emptyTileImage.copy()
                }
            }
        }
    }

    /**
     * Implements UI functions for mirror and rotate
     */
    fun setComponentFunctions(){
        /*
        menuScene.saveGameButton.onMouseClicked = {
            rootService.gameService.saveGame("Test")
        }

        menuScene.botSpeedButton.onMouseClicked = {
            if(menuScene.botSpeedButton.text == "Bot Speed default"){
                menuScene.botSpeedButton.text = "Bot Speed fast"
                rootService.botService.botDelay = 300
            }
            else if(menuScene.botSpeedButton.text == "Bot Speed fast"){
                menuScene.botSpeedButton.text = "Bot Speed super fast"
                rootService.botService.botDelay = 100
            }
            else if(menuScene.botSpeedButton.text == "Bot Speed super fast"){
                menuScene.botSpeedButton.text = "Bot Speed slow"
                rootService.botService.botDelay = 1000
            }
            else if(menuScene.botSpeedButton.text == "Bot Speed slow"){
                menuScene.botSpeedButton.text = "Bot Speed default"
                rootService.botService.botDelay = 700
            }

        }
        menuScene.undoButton.onMouseClicked = {
            rootService.playerActionService.undo()
        }
        menuScene.redoButton.onMouseClicked = {
            rootService.playerActionService.redo()
        }
         */
        menuScene.mirrorButton.onMouseClicked = {
            if(menuScene.blockIsEditable && menuScene.currentlySelectedBlock != null) {
                val currentBlokusBlock = checkNotNull(menuScene.currentlySelectedBlock)
                val gameHistory = checkNotNull(rootService.currentGame)
                val game = gameHistory.gameStates[gameHistory.currentStateIndex]
                val currentlyActivePlayer = checkNotNull(
                    game.players.find { player -> player.name == menuScene.currentPlayer.text }
                )
                val currentBlock = currentlyActivePlayer.playerBlocks.find { pBlock ->
                    (pBlock.blockType == currentBlokusBlock.blockType)
                            && (pBlock.color == currentBlokusBlock.color)
                }
                checkNotNull(currentBlock)
                rootService.playerActionService.mirror(currentBlock)
                menuScene.updateSelectedBlock(false)
                if(currentBlock.isMirrored){
                    menuScene.isMirroredLabel.text = "mirrored"
                }
                else{
                    menuScene.isMirroredLabel.text = "not mirrored"
                }
            }
        }
        menuScene.rotateButton.onMouseClicked = {
            if(menuScene.blockIsEditable && menuScene.currentlySelectedBlock != null) {
                val currentBlokusBlock = checkNotNull(menuScene.currentlySelectedBlock)
                val gameHistory = checkNotNull(rootService.currentGame)
                val game = gameHistory.gameStates[gameHistory.currentStateIndex]
                val currentlyActivePlayer = checkNotNull(
                    game.players.find { player -> player.name == menuScene.currentPlayer.text }
                )
                val currentBlock = currentlyActivePlayer.playerBlocks.find { pBlock ->
                    (pBlock.blockType == currentBlokusBlock.blockType)
                            && (pBlock.color == currentBlokusBlock.color)
                }
                checkNotNull(currentBlock)
                rootService.playerActionService.rotate(currentBlock)
                menuScene.updateSelectedBlock(false)
                if(currentBlock.rotation == Rotation.RIGHT){
                    menuScene.isRotatedLabel.text = "rotated 90°"
                }
                else if(currentBlock.rotation == Rotation.BOTTOM){
                    menuScene.isRotatedLabel.text = "rotated 180°"
                }
                else if(currentBlock.rotation == Rotation.LEFT){
                    menuScene.isRotatedLabel.text = "rotated 270°"
                }
                else{
                    menuScene.isRotatedLabel.text = "not rotated"
                }
            }
        }
        //The drag and drop functionality is currently managed in drawMainPlayerHandy()
    }

    /**
     * Implements UI functions for other buttons
     */
    fun setButtonComponentFunctions(){
        menuScene.saveGameButton.onMouseClicked = {
            rootService.gameService.saveGame("Test")
        }

        menuScene.botSpeedButton.onMouseClicked = {
            if(menuScene.botSpeedButton.text == "Bot Speed default"){
                menuScene.botSpeedButton.text = "Bot Speed fast"
                rootService.botService.botDelay = 300
            }
            else if(menuScene.botSpeedButton.text == "Bot Speed fast"){
                menuScene.botSpeedButton.text = "Bot Speed super fast"
                rootService.botService.botDelay = 100
            }
            else if(menuScene.botSpeedButton.text == "Bot Speed super fast"){
                menuScene.botSpeedButton.text = "Bot Speed slow"
                rootService.botService.botDelay = 1000
            }
            else if(menuScene.botSpeedButton.text == "Bot Speed slow"){
                menuScene.botSpeedButton.text = "Bot Speed default"
                rootService.botService.botDelay = 700
            }

        }
        menuScene.undoButton.onMouseClicked = {
            rootService.playerActionService.undo()
        }
        menuScene.redoButton.onMouseClicked = {
            rootService.playerActionService.redo()
        }
    }

    /**
     * Implements UI functions for hot keys
     */
    fun setHotKeyComponentFunction(){
        /*
        //When you press on another players name you see his Blocks
        for(i in menuScene.otherPlayers.indices){
            menuScene.otherPlayers[i].onMouseClicked = {
                if (menuScene.otherPlayerArea.isVisible){
                    menuScene.hideOtherPlayerTiles()
                } else {
                    menuScene.showOtherPlayerTiles(menuScene.otherPlayers[i].text)
                }
            }
        }
        menuScene.gameBoard.dropAcceptor = {_ -> true }

         */

        //Manages the logic to modify blocks (the selected block) with q,e and f
        menuScene.onKeyPressed = { keyPress ->
            //println("Keypressed")
            if(menuScene.blockIsEditable && menuScene.currentlySelectedBlock != null) {
                val currentBlokusBlock = checkNotNull(menuScene.currentlySelectedBlock)
                val gameHistory = checkNotNull(rootService.currentGame)
                val game = gameHistory.gameStates[gameHistory.currentStateIndex]
                val currentlyActivePlayer = checkNotNull(
                    game.players.find{player -> player.name == menuScene.currentPlayer.text}
                )
                val currentBlock = checkNotNull(currentlyActivePlayer.playerBlocks.find {
                        pBlock -> (pBlock.blockType == currentBlokusBlock.blockType)
                        && (pBlock.color == currentBlokusBlock.color)
                })
                //checkNotNull(currentBlock)
                if(keyPress.keyCode == KeyCode.E){
                    rootService.playerActionService.rotate(currentBlock)
                    menuScene.updateSelectedBlock(false)
                    if(currentBlock.rotation == Rotation.RIGHT){
                        menuScene.isRotatedLabel.text = "rotated 90°"
                    }
                    else if(currentBlock.rotation == Rotation.BOTTOM){
                        menuScene.isRotatedLabel.text = "rotated 180°"
                    }
                    else if(currentBlock.rotation == Rotation.LEFT){
                        menuScene.isRotatedLabel.text = "rotated 270°"
                    }
                    else{
                        menuScene.isRotatedLabel.text = "not rotated"
                    }
                } else if(keyPress.keyCode == KeyCode.Q){
                    rootService.playerActionService.rotate(currentBlock)
                    rootService.playerActionService.rotate(currentBlock)
                    rootService.playerActionService.rotate(currentBlock)
                    menuScene.updateSelectedBlock(false)
                    if(currentBlock.rotation == Rotation.RIGHT){
                        menuScene.isRotatedLabel.text = "rotated 90°"
                    }
                    else if(currentBlock.rotation == Rotation.BOTTOM){
                        menuScene.isRotatedLabel.text = "rotated 180°"
                    }
                    else if(currentBlock.rotation == Rotation.LEFT){
                        menuScene.isRotatedLabel.text = "rotated 270°"
                    }
                    else{
                        menuScene.isRotatedLabel.text = "not rotated"
                    }
                }
                else if(keyPress.keyCode == KeyCode.F){
                    rootService.playerActionService.mirror(currentBlock)
                    menuScene.updateSelectedBlock(false)
                    if(currentBlock.isMirrored){
                        menuScene.isMirroredLabel.text = "mirrored"
                    }
                    else{
                        menuScene.isMirroredLabel.text = "not mirrored"
                    }
                }
            }
        }
    }

    /**
     * Implements UI functions to show other tiles
     */
    fun setShowOtherTilesFunction(){
        //When you press on another players name you see his Blocks
        for(i in menuScene.otherPlayers.indices){
            menuScene.otherPlayers[i].onMouseClicked = {
                hideOtherPlayerTiles()
                if(mainBlocks == 0){
                    menuScene.showOtherPlayerTiles(menuScene.otherPlayers[i].text, true)
                    mainBlocks = 1
                } else if (mainBlocks == 1){
                    menuScene.showOtherPlayerTiles(menuScene.otherPlayers[i].text, false)
                    mainBlocks = 2
                } else {

                    mainBlocks = 0
                }
            }
        }
        menuScene.gameBoard.dropAcceptor = {_ -> true }

        menuScene.currentPlayer.onMouseClicked = {
            hideOtherPlayerTiles()
            if(mainBlocks == 0) {
                menuScene.showOtherPlayerTiles(menuScene.currentPlayer.text, true)
                mainBlocks = 1
            } else if(mainBlocks == 1) {
                menuScene.showOtherPlayerTiles(menuScene.currentPlayer.text, false)
                mainBlocks = 2
            } else {

                mainBlocks = 0
            }

        }
    }

    /**
     * Hides all Blocks from the otherPlayerArea and shows the buttons underneath the area (if they exist)
     */
    fun hideOtherPlayerTiles(){
        for (blockType in BlockType.entries){
            menuScene.otherPlayerArea.removeBlock(blockType)
        }
        menuScene.otherPlayerArea.isVisible = false
        menuScene.otherPlayerArea.isDisabled = true
        if(rootService.networkService.client == null) {
            for (i in menuScene.optionalButtons.indices) {
                menuScene.optionalButtons[i].isVisible = true
                menuScene.optionalButtons[i].isDisabled = false
            }
        }
    }
}