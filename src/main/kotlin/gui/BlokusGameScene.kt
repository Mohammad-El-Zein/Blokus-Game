package gui

import entity.*
import service.Refreshable
import service.RootService
import tools.aqua.bgw.animation.MovementAnimation
import tools.aqua.bgw.animation.SequentialAnimation
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.event.MouseEvent
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import kotlin.collections.indices

/**
 * The main game Scene which displays the [entity.BlokusGame]. Assumes there is a game
 * at the creation time of this object
 */
//TODO(Gucken wie man performance mäßig alle Blöcke preloaden könnte)
class BlokusGameScene(val rootService: RootService):
    BoardGameScene(1920,1080), Refreshable {
    /**
     * Is initialized to be the [entity.Block] side length in the current gamemode
     * (Since the gameBoard is fixed size)
     */
    var blockSideLength = 0
    //Can currently be ignored
    val shadowFps = 5
    //Can currently be ignored
    var lastMeasuredTime = 1L
    val yellowTileImage = ImageVisual("YellowTile.png")
    val redTileImage = ImageVisual("RedTile.png")
    val blueTileImage = ImageVisual("BlueTile.png")
    val greenTileImage = ImageVisual("GreenTile.png")
    //val emptyTileImage = ImageVisual("EmptyTile.png")
    val transparentTileImage = ImageVisual("Transparent.png")

    val gameSceneComponents = GameSceneComponents(rootService,this)

    /**
     * Helper flag to know if keyBinds to rotate apply to a block or can be ignored
     */
    var blockIsEditable = false
    /**
     * Stores the currently selected Block. Is null if there is none selected
     */
    var currentlySelectedBlock: BlokusBlock? = null
    /**
     * The x coordinate of the last Mouseclick on an playerBlock
     */
    var lastPressedX = 0
    /**
     * The y coordinate of the last Mouseclick on an playerBlock
     */
    var lastPressedY = 0
    /**
     * The name for the focused player
     * In online this is always the "local" player, otherwise the current player
     */
    val currentPlayer = Label(
        posX = 20,
        posY = 980,
        width = 500,
        height = 80,
        visual = ColorVisual.LIGHT_GRAY
    )

    /**
     * The names of the remaining players
     */
    val otherPlayers = mutableListOf<Label>()

    /**
     * Button to save the game
     */
    val saveGameButton = Button(
        posX = 1430,
        posY = 170,
        width = 440,
        height = 120,
        text = "Save Game"
    )
    /**
     * Button to undo last action
     */
    val undoButton = Button(
        //1500 without botSpeedButton
        posX = 1430,
        posY = 470,
        width = 120,
        height = 120,
        text = "Undo"
        //visual = ImageVisual("UndoButton.png", width = 120, height = 120,0,0)
    )
    /**
     * Button to toggle the bot simulation speed between slow, normal and fast
     */
    val botSpeedButton = Button(
        posX = 1590,
        posY = 470,
        width = 120,
        height = 120,
        text = "Bot Speed default"
        //visual = ImageVisual("UndoButton.png", width = 120, height = 120,0,0)
    )
    /**
     * Button to redo the last undone action
     */
    val redoButton = Button(
        posX = 1750,
        posY = 470,
        width = 120,
        height = 120,
        text = "Redo"
        //visual = ImageVisual("RedoButton.png", width = 120, height = 120,0,0)
    )

    /**
     * Button to rotate the chosen block
     */
    val rotateButton = Button(
        //1500 without botSpeedButton
        posX = 1430,
        posY = 320,
        width = 180,
        height = 120,
        text = "Rotate (Q, E)"
        //visual = ImageVisual("UndoButton.png", width = 120, height = 120,0,0)
    )

    /**
     * Button to mirror the chosen block
     */
    val mirrorButton = Button(
        posX = 1700,
        posY = 320,
        width = 180,
        height = 120,
        text = "Mirror (F)"
        //visual = ImageVisual("RedoButton.png", width = 120, height = 120,0,0)
    )

    /**
     * Label to show if the block is mirrored
     */
    val isMirroredLabel = Button(
        posX = 1430,
        posY = 875,
        width = 150,
        height = 60,
        text = "not mirrored"
    )

    /**
     * Label to show if the block is rotated
     */
    //todo ismirrored als variable einfügen -> viel einfacher
    val isRotatedLabel = Button(
        posX = 1700,
        posY = 875,
        width = 150,
        height = 60,
        text = "not rotated"
    )

    /**
     * An Area where a selected Block is displayed
     */
    var previewArea = Area<BlokusTile>(

    )

    /**
     * The Gameboard as a collection of [BlokusTile]'s
     */
    var gameBoard = GridPane<BlokusTile>(
        posX = 540,
        posY = 120,
        columns = 0,
        rows = 0,
        layoutFromCenter = false,
        visual = ColorVisual.LIGHT_GRAY
    )

    /**
     * The Area where the focused player's Blocks are displayed
     */
    val currentPlayerArea = PlayerArea(
        posX = 20,
        posY = 140,
        width = 500,
        height = 800,
        visual = ColorVisual.DARK_GRAY
    )

    /**
     * The Area where the current player can choose to view another players Blocks
     */
    val otherPlayerArea = PlayerArea(
        posX = 1400,
        posY = 140,
        width = 500,
        height = 800,
        visual = ColorVisual.DARK_GRAY
    )

    /**
     * Collection of the Buttons shown at the right of the game. In online game this is empty
     */
    val optionalButtons = mutableListOf<Button>()

    /**
     * A background Visual behind the optional buttons and the otherPlayerArea
     */
    val optionalButtonArea = Pane<ComponentView>(
        posX = 1400,
        posY = 140,
        width = 500,
        height = 800,
        visual = ColorVisual.LIGHT_GRAY
    )
    init {
        background = ImageVisual(
            "GameSceneBackground.png",
            width = 1920,
            height = 1080,
            offsetX = 0,
            offsetY = 0
        )
    }
    /**
     * Creates the initial gamescene and initializes it with the current game state
     */
    override fun refreshAfterCreateGame() {
        println("gui - game refreshAfterCreateGame")
        val game = rootService.currentGame
        checkNotNull(game) { "No game running" }

        blockSideLength = 42

        gameBoard = GridPane(
            posX = 960,
            posY = 540,
            columns = if (game.blokusGameState.gameMode == GameMode.TWO_PLAYER_SMALL)  14 else 20,
            rows = if (game.blokusGameState.gameMode == GameMode.TWO_PLAYER_SMALL)  14 else 20,
            layoutFromCenter = true,
            visual = ColorVisual.LIGHT_GRAY
        )
        previewArea = Area(
            posX = (optionalButtonArea.width-(5*blockSideLength+40))/2,
            posY = 470,
            width = 5*blockSideLength+40,
            height = 5*blockSideLength+40,
            visual = ColorVisual(10,10,10, alpha = 0.2)
        )
        optionalButtonArea.add(previewArea)

        optionalButtons.addAll(mutableListOf(mirrorButton, rotateButton, botSpeedButton,
            isMirroredLabel, isRotatedLabel))

    if(rootService.networkService.client == null) {
        optionalButtons.addAll(
            mutableListOf(
                saveGameButton,
                redoButton, undoButton
            )
        )
    }
        initializeOtherPlayers()
        gameSceneComponents.createDrawBoard()
        drawMainPlayerHand()

        otherPlayerArea.isVisible = false
        otherPlayerArea.isDisabled = true
        currentPlayerArea.isVisible = true
        currentPlayerArea.isDisabled = false
        undoButton.isDisabled = true
        redoButton.isDisabled = true
        isMirroredLabel.isDisabled = true
        isRotatedLabel.isDisabled = true

        gameSceneComponents.setComponentFunctions()
        gameSceneComponents.setHotKeyComponentFunction()
        gameSceneComponents.setButtonComponentFunctions()
        gameSceneComponents.setShowOtherTilesFunction()
        addComponents(currentPlayer,currentPlayerArea,optionalButtonArea,otherPlayerArea,gameBoard)
        for(i in otherPlayers.indices){
            addComponents(otherPlayers[i])
        }
        /*
        if(rootService.networkService.client == null) {
            for (i in optionalButtons.indices) {
                addComponents(optionalButtons[i])
            }
        }

         */
        for (i in optionalButtons.indices) {
            addComponents(optionalButtons[i])
        }
        /*
        if(rootService.networkService.client == null) {
            for (i in optionalButtons.indices) {
                addComponents(optionalButtons[i])
            }
        }

         */
        refreshAfterEndTurn()
    }

    /*
    /**
     * Implements all UI functions on all components
     */
    fun setComponentFunctions(){
        saveGameButton.onMouseClicked = {
            rootService.gameService.saveGame("Test")
        }

        botSpeedButton.onMouseClicked = {
            if(botSpeedButton.text == "Bot Speed default"){
                botSpeedButton.text = "Bot Speed fast"
                rootService.botService.botDelay = 300
            }
            else if(botSpeedButton.text == "Bot Speed fast"){
                botSpeedButton.text = "Bot Speed super fast"
                rootService.botService.botDelay = 100
            }
            else if(botSpeedButton.text == "Bot Speed super fast"){
                botSpeedButton.text = "Bot Speed slow"
                rootService.botService.botDelay = 1000
            }
            else if(botSpeedButton.text == "Bot Speed slow"){
                botSpeedButton.text = "Bot Speed default"
                rootService.botService.botDelay = 700
            }

        }
        undoButton.onMouseClicked = {
            rootService.playerActionService.undo()
        }
        redoButton.onMouseClicked = {
            rootService.playerActionService.redo()
        }
        mirrorButton.onMouseClicked = {
            if(blockIsEditable && currentlySelectedBlock != null) {
                val currentBlokusBlock = checkNotNull(currentlySelectedBlock)
                val gameHistory = checkNotNull(rootService.currentGame)
                val game = gameHistory.gameStates[gameHistory.currentStateIndex]
                val currentlyActivePlayer = checkNotNull(
                    game.players.find { player -> player.name == currentPlayer.text }
                )
                val currentBlock = currentlyActivePlayer.playerBlocks.find { pBlock ->
                    (pBlock.blockType == currentBlokusBlock.blockType)
                            && (pBlock.color == currentBlokusBlock.color)
                }
                checkNotNull(currentBlock)
                rootService.playerActionService.mirror(currentBlock)
                updateSelectedBlock(false)
                if(currentBlock.isMirrored){
                    isMirroredLabel.text = "mirrored"
                }
                else{
                    isMirroredLabel.text = "not mirrored"
                }
            }
        }
        rotateButton.onMouseClicked = {
            if(blockIsEditable && currentlySelectedBlock != null) {
                val currentBlokusBlock = checkNotNull(currentlySelectedBlock)
                val gameHistory = checkNotNull(rootService.currentGame)
                val game = gameHistory.gameStates[gameHistory.currentStateIndex]
                val currentlyActivePlayer = checkNotNull(
                    game.players.find { player -> player.name == currentPlayer.text }
                )
                val currentBlock = currentlyActivePlayer.playerBlocks.find { pBlock ->
                    (pBlock.blockType == currentBlokusBlock.blockType)
                            && (pBlock.color == currentBlokusBlock.color)
                }
                checkNotNull(currentBlock)
                rootService.playerActionService.rotate(currentBlock)
                updateSelectedBlock(false)
                if(currentBlock.rotation == Rotation.RIGHT){
                    isRotatedLabel.text = "rotated 90°"
                }
                else if(currentBlock.rotation == Rotation.BOTTOM){
                    isRotatedLabel.text = "rotated 180°"
                }
                else if(currentBlock.rotation == Rotation.LEFT){
                    isRotatedLabel.text = "rotated 270°"
                }
                else{
                    isRotatedLabel.text = "not rotated"
                }
            }
        }
        //The drag and drop functionality is currently managed in drawMainPlayerHandy()
    }

    fun setHotKeyComponentFunction(){
        //When you press on another players name you see his Blocks
        for(i in otherPlayers.indices){
            otherPlayers[i].onMouseClicked = {
                if (otherPlayerArea.isVisible){
                    hideOtherPlayerTiles()
                } else {
                    showOtherPlayerTiles(otherPlayers[i].text)
                }
            }
        }
        gameBoard.dropAcceptor = {_ -> true }

        //Manages the logic to modify blocks (the selected block) with q,e and f
        onKeyPressed = { keyPress ->
            println("Keypressed")
            if(blockIsEditable && currentlySelectedBlock != null) {
                val currentBlokusBlock = checkNotNull(currentlySelectedBlock)
                val gameHistory = checkNotNull(rootService.currentGame)
                val game = gameHistory.gameStates[gameHistory.currentStateIndex]
                val currentlyActivePlayer = checkNotNull(
                    game.players.find{player -> player.name == currentPlayer.text}
                )
                val currentBlock = currentlyActivePlayer.playerBlocks.find {
                        pBlock -> (pBlock.blockType == currentBlokusBlock.blockType)
                        && (pBlock.color == currentBlokusBlock.color)
                }
                checkNotNull(currentBlock)
                if(keyPress.keyCode == KeyCode.E){
                    rootService.playerActionService.rotate(currentBlock)
                    updateSelectedBlock(false)
                    if(currentBlock.rotation == Rotation.RIGHT){
                        isRotatedLabel.text = "rotated 90°"
                    }
                    else if(currentBlock.rotation == Rotation.BOTTOM){
                        isRotatedLabel.text = "rotated 180°"
                    }
                    else if(currentBlock.rotation == Rotation.LEFT){
                        isRotatedLabel.text = "rotated 270°"
                    }
                    else{
                        isRotatedLabel.text = "not rotated"
                    }
                } else if(keyPress.keyCode == KeyCode.Q){
                    rootService.playerActionService.rotate(currentBlock)
                    rootService.playerActionService.rotate(currentBlock)
                    rootService.playerActionService.rotate(currentBlock)
                    updateSelectedBlock(false)
                    if(currentBlock.rotation == Rotation.RIGHT){
                        isRotatedLabel.text = "rotated 90°"
                    }
                    else if(currentBlock.rotation == Rotation.BOTTOM){
                        isRotatedLabel.text = "rotated 180°"
                    }
                    else if(currentBlock.rotation == Rotation.LEFT){
                        isRotatedLabel.text = "rotated 270°"
                    }
                    else{
                        isRotatedLabel.text = "not rotated"
                    }
                }
                else if(keyPress.keyCode == KeyCode.F){
                    rootService.playerActionService.mirror(currentBlock)
                    updateSelectedBlock(false)
                    if(currentBlock.isMirrored){
                        isMirroredLabel.text = "mirrored"
                    }
                    else{
                        isMirroredLabel.text = "not mirrored"
                    }
                }
            }
        }
    }
    */

    /*
    /**
     * Enables or disables the load game button depending on whether a saved game is present.
     */
    fun updateButtons() {
        val game = checkNotNull( rootService.currentGame)
        undoButton.isDisabled = game.currentStateIndex <= 0
        redoButton.isDisabled = game.currentStateIndex >= game.gameStates.size -1
    }
     */

    /**
     * Updates and shows the hand of the given other player
     *
     * @param playerName the name of the player whose Blocks you want to be shown
     */
    fun showOtherPlayerTiles(playerName: String, mainBlocks: Boolean){
        val gameHistory = checkNotNull(rootService.currentGame)
        val game = gameHistory.gameStates[gameHistory.currentStateIndex]

        val selectedPlayer = checkNotNull(
            game.players.find{player -> player.name == playerName}
        )
        for(i in selectedPlayer.playerBlocks.indices){
            //TODO(Welche der Farben soll angezeigt werden?)
            if(mainBlocks){
                if(selectedPlayer.playerBlocks[i].color == selectedPlayer.playerBlocks[0].color) {
                    otherPlayerArea.addBlock(buildBlokusBlock(selectedPlayer.playerBlocks[i]))
                }
            } else {
                if(selectedPlayer.playerBlocks[i].color == selectedPlayer.playerBlocks[
                        selectedPlayer.playerBlocks.size-1
                ].color) {
                    otherPlayerArea.addBlock(buildBlokusBlock(selectedPlayer.playerBlocks[i]))
                }
            }

        }
        otherPlayerArea.isVisible = true
        otherPlayerArea.isDisabled = false
        for(i in optionalButtons.indices){
            optionalButtons[i].isVisible = false
            optionalButtons[i].isDisabled = true
        }
    }

    /*
    /**
     * Hides all Blocks from the otherPlayerArea and shows the buttons underneath the area (if they exist)
     */
    fun hideOtherPlayerTiles(){
        for (blockType in BlockType.entries){
            otherPlayerArea.removeBlock(blockType)
        }
        otherPlayerArea.isVisible = false
        otherPlayerArea.isDisabled = true
        if(rootService.networkService.client == null) {
            for (i in optionalButtons.indices) {
                optionalButtons[i].isVisible = true
                optionalButtons[i].isDisabled = false
            }
        }
    }
     */

    /**
     * Sets the name Labels of the other players depending on how many there are
     */
    fun initializeOtherPlayers(){
        val gameHistory = checkNotNull(rootService.currentGame)
        val game = gameHistory.gameStates[gameHistory.currentStateIndex]
        currentPlayer.text = if(rootService.networkService.client?.playerName?.isNotEmpty() == true){
            val client = checkNotNull(rootService.networkService.client)
            client.playerName
        } else {
            rootService.gameService.getCurrentPlayer().name
        }
        val numOfOtherPlayers = game.players.size-1
        val labelWidth = 500 / numOfOtherPlayers
        var currentOtherPlayerNum = 0
        for (i in 0.. numOfOtherPlayers){
            if(game.players[i].name != currentPlayer.text) {
                otherPlayers.add(Label(
                    posX = 1400 + labelWidth*(currentOtherPlayerNum),
                    posY = 980,
                    width = labelWidth,
                    height = 80,
                    text = game.players[i].name,
                    visual = ColorVisual.LIGHT_GRAY
                    ))
                currentOtherPlayerNum++
            }
        }
    }


    /**
     * After a block has been placed the board is updated.
     * (PlayerView is not updated since it is updated in refreshAfterEndTurn)
     */
    override fun refreshAfterBlockPlaced() {
        gameSceneComponents.drawGameBoard()
        println("gui - game refreshAfterBlockPlaced")
        //updateButtons()
        gameSceneComponents.updateButtons()
    }

    /**
     * After a turn there is checked who the new currentPlayer is.
     * In offline the players are switched around and the currentPlayer is focused.
     * In online the players stay in the same order but the currentPlayer gets marked visually
     * If the focused player is the currentPlayer the Blocks are made interactable, otherwise not
     */
    override fun refreshAfterEndTurn() {
        println("gui - game refreshAfterEndTurn")
        //Delete Block Preview
        currentlySelectedBlock = null
        blockIsEditable = false
        updateSelectedBlock(true)

        val gameHistory = checkNotNull(rootService.currentGame)
        val game = gameHistory.gameStates[gameHistory.currentStateIndex]

        gameSceneComponents.hideOtherPlayerTiles()
        gameSceneComponents.mainBlocks = 0
        //Change name order in offline Game
        if (rootService.networkService.client == null) {
            currentPlayer.text = rootService.gameService.getCurrentPlayer().name
            var tmpPlayeIdx = 0
            for (i in otherPlayers.indices){
                if(game.players[i].name == currentPlayer.text) {
                    tmpPlayeIdx++
                }
                otherPlayers[i].text = game.players[tmpPlayeIdx].name
                tmpPlayeIdx++
            }
        }

        //Mark the currently active player
        for (i in otherPlayers.indices){
            if (otherPlayers[i].text == rootService.gameService.getCurrentPlayer().name){
                otherPlayers[i].visual = ColorVisual.GREEN
            } else {
                otherPlayers[i].visual = ColorVisual.LIME
            }
        }
        if (currentPlayer.text == rootService.gameService.getCurrentPlayer().name){
            currentPlayer.visual = ColorVisual.GREEN
        } else {
            currentPlayer.visual = ColorVisual.LIME
        }
        drawMainPlayerHand()

        val allLabels = otherPlayers.toMutableList()
        allLabels.add(currentPlayer)
        val animatedLabel = allLabels.find { it.text == rootService.gameService.getCurrentPlayer().name}
        println("MaybePlayAnimation")
        if(animatedLabel != null){
            println("PlayAnimation")
            playAnimation(SequentialAnimation(
                MovementAnimation(componentView = animatedLabel, byY = -30, duration = 500),
            ).apply {
                onFinished = {
                    println("Mal sehen")
                }
            })
        }
    }
    /**
     * Shows the current gamestate after redo
     */
    override fun refreshAfterRedo() {
        gameSceneComponents.drawGameBoard()
        refreshAfterEndTurn()
        otherPlayerArea.isDisabled = true
        otherPlayerArea.isVisible = false
        //updateButtons()
        gameSceneComponents.updateButtons()
    }
    /**
     * Shows the current gamestate after undo
     */
    override fun refreshAfterUndo() {
        gameSceneComponents.drawGameBoard()
        refreshAfterEndTurn()
        otherPlayerArea.isDisabled = true
        otherPlayerArea.isVisible = false
        //updateButtons()
        gameSceneComponents.updateButtons()
    }
    /**
     * Draws the blocks of the currently focused player in the [currentPlayerArea]
     * Initializes the drag functionality on the blocks
     */
    fun drawMainPlayerHand(){
        val gameHistory = checkNotNull(rootService.currentGame)
        val game = gameHistory.gameStates[gameHistory.currentStateIndex]

        val currentlyActivePlayer = checkNotNull(
            game.players.find{player -> player.name == currentPlayer.text}
        )
        currentPlayerArea.reset()
        for(i in currentlyActivePlayer.playerBlocks.indices){
            if(rootService.networkService.client==null){
                //Draw current (active) player with color which is active on turn
                if(currentlyActivePlayer.playerBlocks[i].color == game.colorOrder[0]) {
                    currentPlayerArea.addBlock(buildBlokusBlock(currentlyActivePlayer.playerBlocks[i]))
                }
            } else {
                //Draw hosting player with some of his blocks
                if(currentlyActivePlayer.playerBlocks[i].color == game.colorOrder[0]) {
                    currentPlayerArea.addBlock(buildBlokusBlock(currentlyActivePlayer.playerBlocks[i]))
                }
            }
        }
        currentPlayerArea.isVisible = true
        currentPlayerArea.isDisabled = false

        //Disables current player blocks if it is not his turn, but he is focussed because of online
        if ((rootService.networkService.client != null)
            && (currentPlayer.text != rootService.gameService.getCurrentPlayer().name)){
            for (i in currentPlayerArea.currentlyDisplayedBlokus.indices){
                currentPlayerArea.currentlyDisplayedBlokus[i].isDisabled = true
                currentPlayerArea.currentlyDisplayedBlokus[i].isDraggable = false
            }
        } else {
            for (i in currentPlayerArea.currentlyDisplayedBlokus.indices){
                //Create Draggable BlokusBlocks
                currentPlayerArea.currentlyDisplayedBlokus[i].isDisabled = false
                currentPlayerArea.currentlyDisplayedBlokus[i].isDraggable = true
                setBlockFunctions(currentPlayerArea.currentlyDisplayedBlokus[i],currentlyActivePlayer)
            }
        }
    }
    private fun setBlockFunctions(block: BlokusBlock, currentlyActivePlayer: Player){
        block.apply {
            onMousePressed = { pressEvent ->
                lastPressedX = pressEvent.posX.toInt()
                lastPressedY = pressEvent.posY.toInt()
                blockIsEditable = false
                if(currentlySelectedBlock != this) {
                    currentlySelectedBlock = this
                    updateSelectedBlock(true)
                }
            }
            onMouseReleased = { releaseEvent ->
                blockIsEditable = true
                tryPlacingBlock(
                    releaseEvent = releaseEvent,
                    block = this,
                    player = currentlyActivePlayer)
            }
            onDragGestureMoved = { dragEvent ->
                dragEvent.draggedComponent
                val time = System.currentTimeMillis()
                if (time-lastMeasuredTime >= 1000/shadowFps){
                    lastMeasuredTime = time
                    //TODO(Shadowberechnung)
                    // println("Shadow berechnen an Stelle ${dragEvent.draggedComponent.posX}
                    // oder ${dragEvent.draggedComponent.actualPosX}")
                }
            }
        }
    }
    /**
     * Gets called after a selected block has been selected, rotated or mirrored. Updates it's view
     * in focusedPlayerView and in the previewArea
     *
     * @param onlyPreview whether to only update the preview Area or the playerhand too
     */
    fun updateSelectedBlock(onlyPreview : Boolean) {
        if(!onlyPreview)drawMainPlayerHand()
        previewArea.removeAll{true}
        if(currentlySelectedBlock != null) {
            val currentBlokusBlock = checkNotNull(currentlySelectedBlock)
            val gameHistory = checkNotNull(rootService.currentGame)
            val game = gameHistory.gameStates[gameHistory.currentStateIndex]
            val currentlyActivePlayer = checkNotNull(
                game.players.find{player -> player.name == currentPlayer.text}
            )
            val currentBlock = checkNotNull(currentlyActivePlayer.playerBlocks.find {
                    pBlock -> (pBlock.blockType == currentBlokusBlock.blockType)
                    && (pBlock.color == currentBlokusBlock.color)
            })

            val currentUpdatedBlokusBlock = buildBlokusBlock(currentBlock)
            val tiles = currentUpdatedBlokusBlock.components
            val xOffset = ((previewArea.width - currentUpdatedBlokusBlock.width)/2).toInt()
            val yOffset = ((previewArea.height - currentUpdatedBlokusBlock.height)/2).toInt()
            for (tile in tiles) {
                previewArea.add(tile.copy(xOffset, yOffset))
            }
        }
    }
    /**
     * Tries to place a dragged BlokusBlock on the gameBoard where it was dragged. If it cant the Block
     * snaps back to the players hand
     *
     * @param releaseEvent The MouseEvent created when the mouse (dragging the component) released the mouse button
     * @param block the dragged BlokusBlock
     * @param player the currently active player
     *
     */
    private fun tryPlacingBlock(releaseEvent: MouseEvent, block: BlokusBlock,player: Player){
        //println("Tile.onMouseReleased")
        val currentX = releaseEvent.posX.toInt()
        val currentY = releaseEvent.posY.toInt()
        //println("mauszeiger: $currentX,$currentY")
        //Coordinate where top left corner of component would be
        //println("wo block angeclickt wurde: $lastPressedX,$lastPressedY")
        //println("wo blockCorner war${block.actualPosX},${block.actualPosY}")
        var cornerX = currentX - (lastPressedX-block.actualPosX)
        var cornerY = currentY - (lastPressedY-block.actualPosY)
        //println("wo blockCorner jetzt sein soll$cornerX,$cornerY")
        //Relativise to gameBoard
        cornerX -= (gameBoard.posX-(blockSideLength*gameBoard.columns)/2)
        cornerY -= (gameBoard.posY-(blockSideLength*gameBoard.rows)/2)
        //println("wo BlockCorner im Gameboard sein soll $cornerX,$cornerY")
        //TODO(Investigate if rounding or casting to int is better)
        val indexX = ((cornerX+20) / blockSideLength).toInt()
        val indexY = ((cornerY+20) / blockSideLength).toInt()
        //println("Theoretische Indices in der Matrix $indexX,$indexY")
        //println("Praktische Indices in der Matrix $indexX,$indexY")
        val currentBlock = player.playerBlocks.find {
                pBlock -> (pBlock.blockType == block.blockType) && (pBlock.color == block.color)
        }
        checkNotNull(currentBlock) {"Tried to play a block that doesn't exist"}
        /*if(indexX in 0..gameBoard.columns && indexY in 0..gameBoard.rows ) {
            if(rootService.gameService.validatePlacement(xCoord = indexX, yCoord = indexY,  currentBlock)) {
                println("Placing Block")
                rootService.playerActionService.placeBlock(currentBlock,indexX,indexY)
            }*/
        if(indexX in 0..gameBoard.columns && indexY in 0..gameBoard.rows ) {
            if(rootService.gameService.validatePlacement(xCoord = indexX, yCoord = indexY, block =  currentBlock)) {
                println("Placing Block")
                rootService.playerActionService.placeBlock(currentBlock,x = indexX, y = indexY)
            }
        } else {
            println("Out of bounds")
        }

    }
    /*
    /**
     * creates the empty drawBoard for the current boardSize filled with empty tiles
     */
    fun createDrawBoard(){
        val gameHistory = checkNotNull(rootService.currentGame)
        val game = gameHistory.gameStates[gameHistory.currentStateIndex]
        for (i in 0..< game.gameBoard.size-2){
            for (j in 0..< game.gameBoard[i].size-2){
                gameBoard[i, j] = BlokusTile(width = blockSideLength, height = blockSideLength,
                    color = game.gameBoard[i+1][j+1],
                    visual = when(game.gameBoard[i+1][j+1]){
                        entity.Color.YELLOW -> yellowTileImage.copy()
                        entity.Color.RED -> redTileImage.copy()
                        entity.Color.BLUE -> blueTileImage.copy()
                        entity.Color.GREEN -> greenTileImage.copy()
                        else -> emptyTileImage.copy()
                    }
                )
            }
        }
    }
     */
    /*
    /**
     * Updates the [entity.BlokusGameState.gameBoard] with the current contents
     */
    fun drawGameBoard(){
        //TODO(game und gamehistory Sachen auch überall so wie hier erstetzen)
        val gameHistory = checkNotNull(rootService.currentGame)
        val game = gameHistory.gameStates[gameHistory.currentStateIndex]
        for (i in 0..< game.gameBoard.size-2){
            for (j in 0..< game.gameBoard[i].size-2){
                gameBoard[i, j]?.color =   game.gameBoard[i+1][j+1]
                gameBoard[i, j]?.visual = when(game.gameBoard[i+1][j+1]){
                    Color.YELLOW -> yellowTileImage.copy()
                    Color.RED -> redTileImage.copy()
                    Color.BLUE -> blueTileImage.copy()
                    Color.GREEN -> greenTileImage.copy()
                    else -> emptyTileImage.copy()
                }
            }
        }
    }
     */

    /**
     * Helper function to create a [BlokusBlock] from a given [entity.Block]
     *
     * @param block the block to be modeled
     * @return The [BlokusBlock] representation of the given block
     */
    fun buildBlokusBlock(block: Block) : BlokusBlock {
        val matrix = block.tiles.subList(1,block.tiles.size-1).toMutableList()
        for (i in matrix.indices){
            matrix[i] = matrix[i].subList(1,matrix[i].size-1)
        }
        val currentBlock = BlokusBlock(
            blockType = block.blockType,
            color = block.color,
            width = matrix.size*blockSideLength,
            height = matrix[0].size*blockSideLength
        )
        for (i in matrix.indices){
            for (j in matrix[i].indices) {
                currentBlock.add(BlokusTile(
                    posX = i*blockSideLength,
                    posY = j*blockSideLength,
                    width = blockSideLength,
                    height = blockSideLength,
                    color = block.color,
                    visual = when(matrix[i][j]){
                        Color.YELLOW -> yellowTileImage.copy()
                        Color.RED -> redTileImage.copy()
                        Color.BLUE -> blueTileImage.copy()
                        Color.GREEN -> greenTileImage.copy()
                        else -> transparentTileImage.copy()
                    }
                ))
            }
        }
        return currentBlock
    }
}