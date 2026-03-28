package gui

import entity.GameMode
import service.ConnectionState
import service.Refreshable
import service.RootService
import tools.aqua.bgw.core.BoardGameApplication

/**
 * The main Application to start the Blokus Game. Manages the different GUI elements
 */
class BlokusApplication: BoardGameApplication("Blokus Game"), Refreshable {
    //TODO(Change currentPlayer call in GUI when uptdaten in service)
    //TODO(Delete when service provides them)
    /**
     * If the created game is online or offline
     */
    var isOnlineGame = false

    /**
     * The name of the Player who is sitting on the local machine. Empty if offlinegame
     */
    var nameOfLocalPlayer = ""
    /**
     * The root service instance. This is used to call service methods and access the entity layer.
     */
    val rootService: RootService = RootService()



    /**
     * The game scene where the Game will take place
     */
    val gameScene = BlokusGameScene(rootService)

    /**
     * The start screen. Here you can select you gamemode and start games
     */
    val newGameMenuScene = NewGameMenuScene(rootService, this)

    /**
     * The waiting scene you see when you connect to an online game
     */
    val waitingScene = WaitingScene()

    /**
     * The score screen. Here the scores will be displayed
     */
    val scoreScene = ScoreBoardScene(rootService, this)
    /**
     * Initializes the application by displaying the [NewGameMenuScene].
     */
    init {
        this.showMenuScene(newGameMenuScene)
        addOnAllServices(newGameMenuScene)
        addOnAllServices(gameScene)
        addOnAllServices(scoreScene)
        addOnAllServices(this)
    }

    /**
     * Adds the given refrashable to all services
     */
    private fun addOnAllServices(refreshable: Refreshable){
        rootService.gameService.addRefreshable(refreshable)
        rootService.playerActionService.addRefreshable(refreshable)
        rootService.networkService.addRefreshable(refreshable)
        //rootService.botService.addRefreshable(refreshable)

    }
    /**
     * After a game is created the menu scenes are hidden and the GameScene of the game is shown
     */
    override fun refreshAfterCreateGame() {
        this.hideMenuScene()
        this.showGameScene(gameScene)
        println("gui - application refreshAfterCreateGame")
    }

    /**
     * After the user joined a game and he is waiting for the host to start the game
     */
    override fun refreshConnectionState(state: ConnectionState) {
        if(state == ConnectionState.WAITING_FOR_INIT) {
            this.hideMenuScene()
            this.showMenuScene(waitingScene)
        }
        println("gui - application refreshConnectionState")
    }
    /**
     * refreshes after a game has been loaded
     */
    override fun refreshAfterLoadGame() {
        val game = checkNotNull(rootService.currentGame)
        newGameMenuScene.currentGameMode = game.blokusGameState.gameMode
        when(newGameMenuScene.currentGameMode){
            GameMode.TWO_PLAYER_SMALL -> newGameMenuScene.show(newGameMenuScene.twoPlayerSelectionPane)
            GameMode.TWO_PLAYER_FOUR_COLOR -> newGameMenuScene.show(newGameMenuScene.twoPlayerSelectionPane)
            GameMode.THREE_PLAYER -> newGameMenuScene.show(newGameMenuScene.threePlayerSelectionPane)
            GameMode.FOUR_PLAYER -> newGameMenuScene.show(newGameMenuScene.fourPlayerSelectionPane)
        }
        gameScene.refreshAfterCreateGame()
        this.hideMenuScene()
        this.showGameScene(gameScene)

    }
    /**
     * After a player has joined the game of the users hostet game
     */
    //Done this way so that the Panes don't have to be made Refreshables. maybe change in future
    override fun refreshAfterPlayerJoined() {
        newGameMenuScene.currentSelectionPane.refreshAfterPlayerJoined()
        println("gui - application refreshAfterPlayerJoined")
    }
    /**
     * After a game is ended the scoreboard is shown
     */
    override fun refreshAfterGameEnd() {
        hideMenuScene(2)
        showMenuScene(scoreScene)
        println("gui - application refreshAfterGameEnd")
    }
}