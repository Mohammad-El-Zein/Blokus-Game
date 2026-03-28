package gui

import entity.Color
import entity.GameMode
import entity.Player
import entity.PlayerType
import gui.startMenuComponents.MenuType
import gui.startMenuComponents.PlayerSelectionArea
import gui.startMenuComponents.PlayerTypeSelectionBox
import service.Refreshable
import service.RootService
import tools.aqua.bgw.animation.MovementAnimation
import tools.aqua.bgw.animation.ParallelAnimation
import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.components.StaticComponentView
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.container.GameComponentContainer
import tools.aqua.bgw.components.gamecomponentviews.GameComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.components.uicomponents.ToggleButton
import tools.aqua.bgw.components.uicomponents.ToggleGroup
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.Visual
import java.awt.Menu
import kotlin.collections.get
import kotlin.random.Random
import kotlin.text.get

/**
 * The Start menu scene from where you can configure and start games. This includes online hosting of all gamemodes,
 * joining games and playing offline
 */

abstract class GameModeSelectorPane(val menuScene: NewGameMenuScene): Pane<UIComponent>(width = 100, height = 100) {
    /**
     * A flag to store the information if the game should be started with advanced scoring
     */
    var isAdvancedScoring = true

    /**
     * A flag to store the information if the scene should display offline or online configuration options
     */

    /**
     * A flag to store if the gameMode is locked. This is set if the player has specified parameters or configurations
     * which don't give the option two switch the gameMode after. Pretty much only after an online hosting game has been
     * startet
     */
    var gameModeIsLocked  = false

    /**
     *
     */
    var connectedPlayers = 0
    /**
     * A helper variable for generating random values
     */
    val random = Random(1)

    /**
     * A collection of all components which are displayed on in the offline configuration Scene
     */
    val offlineComponents = mutableListOf<UIComponent>()
    /**
     * A collection of all components which are displayed on in the hosting configuration Scene
     */
    val hostingComponents = mutableListOf<UIComponent>()

    /**
     * A collection of all components which are displayed on in the joining configuration Scene
     */
    val joiningComponents = mutableListOf<UIComponent>()

    /**
     * This button creates a game if there are settings provided and all necessary players have sufficient animation
     */
    val startGameButton = Button(
        posX = 1550,
        posY = 920,
        width = 300,
        height = 80,
        text = "Start Game"
    )
    /**
     * This button starts the joining phase for an online game. Disables input until all players have connected
     */
    val startHostingButton = Button(
        posX = 780,
        posY = 760,
        width = 280,
        height = 150,
        text = "Start Hosting"
    )

    /**
     * This buttons changes the flag if the game starts with advanced or normal scoring
     */
    val advancedScoringButton = Button(
        posX = 1550,
        posY = 440,
        width = 300,
        height = 80,
        text = "Advanced Scoring"
    )

    /**
     * Textinput where the bot speed can be configured
     */
    val simulationSpeedInput = TextField(
        posX = 1550,
        posY = 680,
        width = 300,
        height = 80,
        prompt = "Enter Simulation Speed. Standard 2 seconds"
    )

    /**
     * loads a currently saved game
     */
    val loadGameButton = Button(
        posX = 1550,
        posY = 800,
        width = 300,
        height = 80,
        text = "Load Game"
    )

    /**
     * If all necessary players are configured the player order is randomized.
     * If the gameMode allows it the color order is randomized too
     */
    val shuffleOrderButton = Button(
        posX = 1550,
        posY = 560,
        width = 300,
        height = 80,
        text = "Shuffle"
    )

    /**
     * Contains four [PlayerSelectionArea]'s. Depending on game mode some are hidden
     */
    var playerInputs = mutableListOf<PlayerSelectionArea>(

    )

    /**
     * The Textfield where an online Player who hosts or wants to join sets their name
     */
    val onlineName = TextField(
        posX = 600,
        posY=350,
        width = 660,
        height = 152,
        prompt = "Your Name"
    )

    /**
     * The Textfield where an online Player who hosts or wants to join specifies the sessionID
     */
    val onlineSessionID = TextField(
        posX = 620,
        posY = 520,
        width = 440,
        height = 66,
        prompt = "SessionID"
    )

    /**
     * The ComboBox where an online Player who hosts or wants to join sets their playerType
     */
    val onlinePlayerTypeSelector = PlayerTypeSelectionBox(
        posX = 1080,
        posY = 520,
        width = 100,
        height = 66,
        items = listOf(PlayerType.PLAYER, PlayerType.TESTBOT, PlayerType.STOCKFISHWANNABE),
        formatFunction = { enumType -> enumType.toString() }
    )

    /**
     * If a player in the join scene has set all their attributes correctly it connects to a game
     */
    val joinButton = Button(
        posX = 780,
        posY = 760,
        width = 280,
        height = 150,
        text = "join"
    )

    /**
     * Adds all components to the main menu scene and configures the offline, join and hosting lists.
     * Also sets the component UI functions
     */
    init{
        offlineComponents.addAll(mutableListOf(
            startGameButton,simulationSpeedInput,advancedScoringButton,shuffleOrderButton,loadGameButton
        ))
        hostingComponents.add(onlinePlayerTypeSelector)
        hostingComponents.addAll(mutableListOf(
            onlineName,onlineSessionID,startHostingButton
        ))
        joiningComponents.add(onlinePlayerTypeSelector)
        joiningComponents.addAll(mutableListOf(
            onlineName,onlineSessionID,joinButton
        ))

        for (component in offlineComponents){
            add(component)
        }

        addAll(onlineName,onlineSessionID,onlinePlayerTypeSelector,joinButton,startHostingButton)

        initializePlayerAreas()

        addPlayerInputs()

        setComponentFunctions1()
    }
    /**
     * function to initialize tha playerAreas. Gets implemented in subclasses
     */
    abstract fun initializePlayerAreas()
    /**
     * Adds the PlayerSelectionArea components to this pane and to offline Components.
     * Sets the component functions
     * It doesn't get added to hostingComponents at this time
     */
    fun addPlayerInputs(){
        for (playerSelectionArea in playerInputs) {
            for (component in playerSelectionArea.currentComponentList) {
                offlineComponents.add(component)
                add(component)
            }
        }
    }

    /**
     * sets some component UI functions besides playerInputs
     */
    private fun setComponentFunctions1() {
        //Starts and created a game if all parameter are set
        startGameButton.onMouseClicked = {
            var isReadyToStart = true
            for (i in 0..< playerNum(menuScene.currentGameMode)){
                if(!playerInputs[i].isComplete()){
                    isReadyToStart = false
                }
            }
            if(isReadyToStart){
                createGame()
            }

        }
        loadGameButton.onMouseClicked = {
            menuScene.rootService.gameService.loadGame("Test")
        }

        //Toggles the scoring system
        advancedScoringButton.onMouseClicked = {
            isAdvancedScoring = !isAdvancedScoring
            if (isAdvancedScoring) {
                advancedScoringButton.text = "Advanced Scoring"
            } else {
                advancedScoringButton.text = "Normal Scoring"
            }
        }


        //Joins a player if he specified correct parameters. Then switches to waiting screen
        joinButton.onMouseClicked = {
            if(onlineName.text.isNotBlank() && onlineSessionID.text.isNotBlank()){
                menuScene.joinGame(playerName = onlineName.text, sessionID = onlineSessionID.text,
                    playerType = onlinePlayerTypeSelector.getSelected())
            }

        }
        //Starts the hosting of a game if there are correct parameters. Displays the hosts player data and waits
        //for players to connect
        startHostingButton.onMouseClicked = {
            if(onlineName.text.isNotBlank() && onlineSessionID.text.isNotBlank()){
                gameModeIsLocked = true
                menuScene.lockUserInput()
                lockUserInput()
                menuScene.rootService.networkService.hostGame(name = onlineName.text,
                    sessionID = onlineSessionID.text,
                    playerType = onlinePlayerTypeSelector.getSelected())
            }

        }
        setComponenFunctions2()
    }
    /**
     * sets the remaining UI functions besides playerInputs
     */
    private fun setComponenFunctions2(){
        //Shuffles players if all players of the current mode are complete
        //If the mode allows it the colors are shuffled too
        shuffleOrderButton.onMouseClicked = {
            var everythingFilledOut = true
            for(i in 0..< playerNum(menuScene.currentGameMode)){
                if (!playerInputs[i].isComplete()){
                    everythingFilledOut = false
                }
            }
            if(everythingFilledOut) {
                val swapColor = (menuScene.currentGameMode == GameMode.FOUR_PLAYER)
                repeat(5, {
                    playerInputs[random.nextInt( playerNum(menuScene.currentGameMode))].swapContents(
                        playerInputs[random.nextInt( playerNum(menuScene.currentGameMode))], swapColor
                    )
                })
            }
        }

        for(i in playerInputs.indices){
            //Swap with player above if there is one and both are complete
            println("switchButtons für $i initialisieren")
            playerInputs[i].switchDown.onMouseClicked = {
                println("switchUp")
                if(i+1 < playerInputs.size){
                    println("switchUp")
                    if (playerInputs[i].isComplete() && playerInputs[i+1].isComplete()){
                        println("switchUp")
                        playerInputs[i].swapContents(playerInputs[i+1],false)
                    }
                }
            }
            //Swap with player below if there is one and both are complete
            playerInputs[i].switchUp.onMouseClicked = {
                println("switchDown")
                if(i-1 >= 0){
                    println("switchDown")
                    if (playerInputs[i].isComplete() && playerInputs[i-1].isComplete()){
                        println("switchDown")
                        playerInputs[i].swapContents(playerInputs[i-1],false)
                    }
                }
            }
        }
    }
    /**
     * Shows all offlineComponents
     */
    open fun show(pMode: MenuType) {
        when(pMode) {
            MenuType.OFFLINE -> showOfflineOptions()
            MenuType.HOSTING -> showHostingOptions()
            MenuType.JOINING -> showJoiningOptions()
        }
    }

    /**
     * Shows all OfflineComponents
     */
    fun showOfflineOptions() {
        println("ShowOfflineOptions")
        for (comp in joiningComponents) {
            comp.isVisible = false
            comp.isDisabled = true
        }
        for (comp in hostingComponents) {
            comp.isVisible = false
            comp.isDisabled = true
        }
        for (comp in offlineComponents) {
            comp.isVisible = true
            comp.isDisabled = false
        }
        for(playerInput in playerInputs){
            if (playerInput.isColorOnly){
                playerInput.playerNameInput.isVisible = false
                playerInput.playerTypeSelection.isVisible = false
                playerInput.switchUp.isVisible = false
                playerInput.switchDown.isVisible = false
            }
        }
    }

    /**
     * Shows all JoiningComponents
     */
    fun showJoiningOptions(){
        println("ShowJoiningOptions")
        for (comp in offlineComponents) {
            comp.isVisible = false
            comp.isDisabled = true
        }
        for (comp in hostingComponents) {
            comp.isVisible = false
            comp.isDisabled = true
        }
        for (comp in joiningComponents) {
            comp.isVisible = true
            comp.isDisabled = false
        }
    }

    /**
     * shows all HostingComponents
     */
    fun showHostingOptions(){
        println("ShowHostingOptions")
        for (comp in offlineComponents) {
            comp.isVisible = false
            comp.isDisabled = true
        }
        for (comp in joiningComponents) {
            comp.isVisible = false
            comp.isDisabled = true
        }
        for (comp in hostingComponents) {
            comp.isVisible = true
            comp.isDisabled = false
        }
    }

    /**
     * Disables user input for all components on the screen
     */
    fun lockUserInput(){
        onlineSessionID.isVisible = false
        onlineSessionID.isDisabled = true
        startHostingButton.isVisible = false
        startHostingButton.isDisabled = true
        onlineName.isVisible = false
        onlineName.isDisabled = true
        onlinePlayerTypeSelector.isVisible = false
        onlinePlayerTypeSelector.isDisabled = true


        //playerInputs[0].playerTypeSelection.setSelectedTo(onlinePlayerTypeSelector.getSelectedAsInt())

        //Gets displayed but cant be edited
        playerInputs[0].playerNameInput.text = onlineName.text
        playerInputs[0].playerNameInput.isVisible = true
        playerInputs[0].playerNameInput.isDisabled = true
        playerInputs[0].playerTypeSelection.isVisible = true
        playerInputs[0].playerTypeSelection.isDisabled = true
        playerInputs[0].playerTypeSelection.updateSelected(onlinePlayerTypeSelector.selected)
        playerInputs[0].playerColor.isVisible = true
        playerInputs[0].playerColor.isDisabled = true
        playerInputs[0].isVisible = true
        playerInputs[0].isDisabled = true
    }
    /**
     * Gets the number of players in a given gamemode
     *
     * @return the number of players
     */
    fun playerNum(mode: GameMode): Int {
        return when(mode){
            GameMode.TWO_PLAYER_SMALL -> 2
            GameMode.TWO_PLAYER_FOUR_COLOR -> 2
            GameMode.THREE_PLAYER -> 3
            GameMode.FOUR_PLAYER -> 4
        }
    }

    /**
     * Collects the given information to create a game instance.
     * Also stores additional info in [application]
     */
    private fun createGame(){
        val players = mutableListOf<Player>()
        var colorOrder = mutableListOf<Color>()
        for(i in 0..< playerInputs.size) {
            if(playerInputs[i].playerNameInput.isVisible){
                players.add(Player(
                    playerInputs[i].playerNameInput.text,
                    playerInputs[i].playerTypeSelection.getSelected()
                ))
                colorOrder.add(playerInputs[i].currentColor)
            }
        }
        for(i in 0..< playerInputs.size) {
            if (!playerInputs[i].playerNameInput.isVisible) {
                colorOrder.add(playerInputs[i].currentColor)
            }
        }
        //Comply to new createGame assumptions
        if(menuScene.currentGameMode == GameMode.TWO_PLAYER_FOUR_COLOR) {
            players.addAll(players.toMutableList())
            colorOrder = mutableListOf(Color.BLUE,Color.YELLOW, Color.RED,Color.GREEN)
        }
        menuScene.createGame(
            players = players,
            colorOrder = colorOrder,
            isAdvancedScoring = isAdvancedScoring,
        )
    }

    //TODO(Need to somehow test if this works correctly)
    /**
     * Is called on an online host if a player has joined after he has hosted. If
     * it was the last player for this gameMode it enables the game configuration for the host
     */
    fun refreshAfterPlayerJoined() {
        //If the is space for connected player
        val playerCount = connectedPlayers+2
        if (playerNum(menuScene.currentGameMode) >= playerCount) {
            val wasLastPlayer = playerNum(menuScene.currentGameMode) == playerCount

            playerInputs[playerCount-1].playerNameInput.isVisible = true
            playerInputs[playerCount-1].playerNameInput.isDisabled = true
            playerInputs[playerCount-1].playerNameInput.text =
                menuScene.rootService.networkService.networkPlayers.last()
            playerInputs[playerCount-1].playerTypeSelection.isVisible = true
            playerInputs[playerCount-1].playerTypeSelection.isDisabled = true
            playerInputs[playerCount-1].playerColor.isVisible = true
            playerInputs[playerCount-1].playerColor.isDisabled = true
            playerInputs[playerCount-1].isVisible = true
            playerInputs[playerCount-1].isDisabled = true
            println("Der $playerCount te Spieler wurde hinzugefügt")
            println("waslastPlayer ist $wasLastPlayer")
            if(wasLastPlayer){
                enableHostingConfigurationOptions()
            }
            println("gui - modeSelectionPane refreshAfterPlayerJoined")

        }
        connectedPlayers++
    }

    /**
     * This is called when all necessary players have joined a hosts game and enables
     * the lobby setting controls for the host
     */
    open fun enableHostingConfigurationOptions(){
        startGameButton.isVisible = true
        startGameButton.isDisabled = false
        advancedScoringButton.isVisible = true
        advancedScoringButton.isDisabled = false
        simulationSpeedInput.isVisible = true
        simulationSpeedInput.isDisabled = false
        advancedScoringButton.isVisible = true
        advancedScoringButton.isDisabled = false
        shuffleOrderButton.isVisible = true
        shuffleOrderButton.isDisabled = false
        for(i in 0..<playerNum(menuScene.currentGameMode)){
            playerInputs[i].switchUp.isDisabled = false
            playerInputs[i].switchUp.isVisible = true
            playerInputs[i].switchDown.isDisabled = false
            playerInputs[i].switchDown.isVisible = true
        }
        if(menuScene.currentGameMode == GameMode.THREE_PLAYER){
            playerInputs[3].playerColor.isVisible = true
        }
    }
}