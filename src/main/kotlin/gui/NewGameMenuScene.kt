package gui

import entity.Color
import entity.GameMode
import entity.Player
import entity.PlayerType
import gui.startMenuComponents.FourPlayerSelectionPane
import gui.startMenuComponents.MenuType
import gui.startMenuComponents.ThreePlayerSelectionPane
import gui.startMenuComponents.TwoPlayerSelectionPane
import service.Refreshable
import service.RootService
import tools.aqua.bgw.animation.MovementAnimation
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.ToggleButton
import tools.aqua.bgw.components.uicomponents.ToggleGroup
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.visual.ColorVisual

/**
 * The MenuScene in which the user can select to host, join or play an offline BlokusGame
 */
class NewGameMenuScene(val rootService: RootService, val application: BlokusApplication):
    MenuScene(1920,1080), Refreshable {
    /**
     * A flag to store the information if the scene should display offline or online configuration options
     */
    var isOffline = true
    /**
     * A flag to store the information if the scene should display online hosting options or online joining options
     */
    var isHosting = false

    /**
     * The current type of menu to be displayed
     */
    var mode = MenuType.OFFLINE
    /**
     * Stores the currently selected Playmode. Initialized with [GameMode.THREE_PLAYER]
     */
    var currentGameMode = GameMode.FOUR_PLAYER

    /**
     * This button changes the displayed elements to offline Components
     */
    val offlineGameButton = Button(
        posX = 600,
        posY = 50,
        width = 300,
        height = 100,
        text = "Offline",
        visual = ColorVisual(150,150,150)
    )

    /**
     * This button changes the displayed elements to online Components
     */
    val onlineGameButton = Button(
        posX = 900,
        posY = 50,
        width = 300,
        height = 100,
        text = "Online",
        visual = ColorVisual.WHITE
    )

    /**
     * This button changes if there are currently hosting or online Components displayed in the online
     * 'scene'
     */
    val hostOrJoinButton = Button(
        posX = 1200,
        posY = 50,
        width = 300,
        height = 100,
        text = "Joining",
        visual = ColorVisual.WHITE,
    )

    /**
     * The ToggleGroup for the gameModeSelectors
     */
    val gameModeToggleGroup = ToggleGroup()

    /**
     * The Toggle Buttons which select the current game mode
     */
    val gameModeSelectors = mutableListOf(
        ToggleButton(
            posX = 220,
            posY = 240,
            width = 300,
            height = 100,
            text = "2 Players Normal",
            toggleGroup = gameModeToggleGroup
        ),
        ToggleButton(
            posX = 570,
            posY = 240,
            width = 300,
            height = 100,
            text = "2 Players Small Board",
            toggleGroup = gameModeToggleGroup
        ),
        ToggleButton(
            posX = 920,
            posY = 240,
            width = 300,
            height = 100,
            text = "3 Players",
            toggleGroup = gameModeToggleGroup
        ),
        ToggleButton(
            posX = 1270,
            posY = 240,
            width = 300,
            height = 100,
            text = "4 Players",
            isSelected = true,
            toggleGroup = gameModeToggleGroup
        )
    )

    val twoPlayerSelectionPane = TwoPlayerSelectionPane(this)
    val threePlayerSelectionPane = ThreePlayerSelectionPane(this)
    val fourPlayerSelectionPane = FourPlayerSelectionPane(this)
    val paneOptions = listOf(twoPlayerSelectionPane,threePlayerSelectionPane,fourPlayerSelectionPane)
    var currentSelectionPane: GameModeSelectorPane = fourPlayerSelectionPane

    init {
        setComponentFunctions()

        addComponents(offlineGameButton,onlineGameButton,hostOrJoinButton)
        for(button in gameModeSelectors){
            addComponents(button)
        }
        hostOrJoinButton.isVisible = false
        hostOrJoinButton.isDisabled = true


        addComponents(twoPlayerSelectionPane)
        addComponents(threePlayerSelectionPane)
        addComponents(fourPlayerSelectionPane)
        show(currentSelectionPane)

    }

    /**
     * Sets functions on the UI Elements which are directly displayed on this component.
     * Components from [GameModeSelectorPane] are configured somewhere else
     */
    fun setComponentFunctions(){
        //Toggles between offline and online view
        offlineGameButton.onMouseClicked = {
            switchOnlineOffline()
            //Hide ModeSelectors if not offline or joining
        }
        //Toggles between offline and online view
        onlineGameButton.onMouseClicked = {
            switchOnlineOffline()
        }
        //Toggles between Host and Join view (in online view)
        hostOrJoinButton.onMouseClicked = {
            isHosting = !isHosting
            for (selector in gameModeSelectors){
                selector.isVisible = isHosting
                selector.isDisabled = !isHosting
            }
            if(isHosting){
                mode = MenuType.HOSTING
                hostOrJoinButton.text = "Hosting"
                currentSelectionPane.show(mode)
            } else {
                mode = MenuType.JOINING
                hostOrJoinButton.text = "Joining"
                currentSelectionPane.show(mode)
            }
        }
        for(i in gameModeSelectors.indices){
            gameModeSelectors[i].onSelected = {
                currentGameMode = getModeOfIndex(i)
                when(currentGameMode){
                    GameMode.TWO_PLAYER_SMALL -> show(twoPlayerSelectionPane)
                    GameMode.TWO_PLAYER_FOUR_COLOR -> show(twoPlayerSelectionPane)
                    GameMode.THREE_PLAYER -> show(threePlayerSelectionPane)
                    GameMode.FOUR_PLAYER -> show(fourPlayerSelectionPane)
                }
            }
        }
    }

    /**
     * Sets the current gameModeSelectionPane, and displays it. The others are hidden
     */
    fun show(selectionPane: GameModeSelectorPane){
        currentSelectionPane = selectionPane
        for (i in paneOptions.indices){
            paneOptions[i].isVisible = (paneOptions[i] == currentSelectionPane)
            paneOptions[i].isDisabled = (paneOptions[i] != currentSelectionPane)
        }
        currentSelectionPane.show(mode)
    }
    /**
     * When the view changes from online to offline all components are updated to only show
     * the now used components
     */
    private fun switchOnlineOffline(){
        isOffline = !isOffline
        if(isOffline){
            mode = MenuType.OFFLINE
            currentSelectionPane.showOfflineOptions()
            playAnimation(MovementAnimation(
                componentView = hostOrJoinButton,
                fromX = hostOrJoinButton.posX,
                fromY = hostOrJoinButton.posY,
                toX = hostOrJoinButton.posX-hostOrJoinButton.width,
                toY = hostOrJoinButton.posY,
                duration = 1000).apply {
                    onFinished = {
                        hostOrJoinButton.isVisible = false
                        hostOrJoinButton.isDisabled = true
                    }
            })
            offlineGameButton.visual = ColorVisual.GRAY
            onlineGameButton.visual = ColorVisual.WHITE
            for (selector in gameModeSelectors){
                selector.isVisible = true
                selector.isDisabled = false
            }
        } else {
            mode = if(isHosting) MenuType.HOSTING else MenuType.JOINING
            hostOrJoinButton.posX = hostOrJoinButton.posX-hostOrJoinButton.width
            hostOrJoinButton.isVisible = true
            hostOrJoinButton.isDisabled = false

            playAnimation(MovementAnimation(
                componentView = hostOrJoinButton,
                fromX = (hostOrJoinButton.posX),
                fromY = hostOrJoinButton.posY,
                toX = hostOrJoinButton.posX+hostOrJoinButton.width,
                toY = hostOrJoinButton.posY,
                duration = 1000).apply {
                onFinished = {
                    hostOrJoinButton.posX = hostOrJoinButton.posX+hostOrJoinButton.width
                }
            })
            offlineGameButton.visual = ColorVisual.WHITE
            onlineGameButton.visual = ColorVisual.GRAY
            if(isHosting){
                currentSelectionPane.showHostingOptions()
            } else {
                currentSelectionPane.showJoiningOptions()
            }

            for (selector in gameModeSelectors){
                selector.isVisible = isHosting
                selector.isDisabled = !isHosting
            }
        }
    }

    /**
     * Creates a game given the parameters
     *
     * @param players the List of players in playing order
     * @param colorOrder the List of Colors in playing order
     * @param isAdvancedScoring whether to use advanced or normal scoring
     */
    fun createGame(players: MutableList<Player>, colorOrder: MutableList<Color>, isAdvancedScoring: Boolean){
        val playerParameter = mutableListOf<Triple<String, Color, PlayerType>>()
        for (i in players.indices){
            //Die nächste Zeile auskommentieren für alte Funktionalität. Das entfernt duplicates
            if(i <playerNumOfMode(currentGameMode))
            playerParameter.add(Triple(players[i].name,colorOrder[i], players[i].playerType))
        }
        println("gui - $playerParameter")
        application.isOnlineGame = !isOffline
        if(application.isOnlineGame) {
            application.nameOfLocalPlayer = currentSelectionPane.onlineName.text
        }

        rootService.gameService.createGame(
            gameMode = currentGameMode,
            isAdvancedScoring = isAdvancedScoring,
            players = playerParameter
        )
        if (rootService.networkService.client != null){
            rootService.networkService.sendInit(
                isAdvancedScoring = isAdvancedScoring,
                players = playerParameter,
                gamemode = currentGameMode
            )
        }

    }

    /**
     * Joins an existing online game
     *
     * @param playerName the name under which the player joins the game
     * @param sessionID the session ID of the game
     */
    fun joinGame(playerName: String, sessionID: String, playerType: PlayerType) {
        rootService.networkService.joinGame(name = playerName, sessionID =  sessionID, playerType)
    }

    /**
     * Disables all UI Components of this component
     */
    fun lockUserInput(){
        for (i in gameModeSelectors.indices){
            gameModeSelectors[i].isDisabled = true
        }
        offlineGameButton.isDisabled = true
        onlineGameButton.isDisabled = true
        hostOrJoinButton.isDisabled = true
    }
    /**
     * Maps the PlayerSelectionButtons to their corresponding gameModes
     *
     * @return the [GameMode] for the given index
     */
    fun getModeOfIndex(i: Int): GameMode {
        if (i == 0) return GameMode.TWO_PLAYER_FOUR_COLOR
        if (i == 1) return GameMode.TWO_PLAYER_SMALL
        if (i == 2) return GameMode.THREE_PLAYER
        if (i == 3) return GameMode.FOUR_PLAYER
        return GameMode.THREE_PLAYER
    }

    /**
     * Returns the number of players in the given gameMode
     */
    fun playerNumOfMode(gameMode: GameMode): Int {
        return when(gameMode) {
            GameMode.FOUR_PLAYER -> 4
            GameMode.THREE_PLAYER -> 3
            GameMode.TWO_PLAYER_SMALL -> 2
            GameMode.TWO_PLAYER_FOUR_COLOR -> 2
        }
    }
}