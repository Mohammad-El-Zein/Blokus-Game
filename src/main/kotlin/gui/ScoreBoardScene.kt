package gui

import service.Refreshable
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.Color
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * The scoreboard scene where the player scores are displayed
 */

// Todo Reihenfolge der Namen nach Punkten anzeigen
class ScoreBoardScene(val rootService: RootService, private val app: BlokusApplication,) :
    MenuScene(2000, 1080), Refreshable {

    private val headlineLabel = Label(
        width = 300, height = 50, posX = 850, posY = 50,
        text = "Game Over",
        font = Font(size = 22),
        alignment = Alignment.CENTER
    )

    // Label for first place
    private val firstName = Label(width = 300, height = 100, posX = 850, posY = 150, text = "First Name",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x26392F)))

    private val firstRank = Label(width = 300, height = 100, posX = 450, posY = 150, text = "1",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xEEC900)))

    private val firstScore = Label(width = 300, height = 100, posX = 1250, posY = 150, text = "First Score",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x26392F)))

    // Label for second place
    private val secondName = Label(width = 300, height = 100, posX = 850, posY = 260, text = "Second Name",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x26392F)))

    private val secondRank = Label(width = 300, height = 100, posX = 450, posY = 260, text = "2",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xC1CDC1)))

    private val secondScore = Label(width = 300, height = 100, posX = 1250, posY = 260, text = "Second Score",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x26392F)))

    // label for third place
    private val thirdName = Label(width = 300, height = 100, posX = 850, posY = 370, text = "Third Name",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x26392F)))

    private val thirdRank = Label(width = 300, height = 100, posX = 450, posY = 370, text = "3",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0xCD9B1D)))

    private val thirdScore = Label(width = 300, height = 100, posX = 1250, posY = 370, text = "Third Score",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x26392F)))

    // label for fourth place
    private val fourthName = Label(width = 300, height = 100, posX = 850, posY = 480, text = "Fourth Name",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x26392F)))

    private val fourthRank = Label(width = 300, height = 100, posX = 450, posY = 480, text = "4",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x8B8878)))

    private val fourthScore = Label(width = 300, height = 100, posX = 1250, posY = 480, text = "Fourth Score",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x26392F)))

    // Label to show if it is advanced Scoring
    private val advancedScoring = Label(width = 300, height = 35, posX = 50, posY = 50, text = "Advanced Scoring",
        alignment = Alignment.CENTER,
        font = Font(22, Color(0xFFFFFFF), "JetBrains Mono ExtraBold"),
        visual = ColorVisual(Color(0x26392F)))

    // Button for quitting the game
    val quitButton = Button(width = 200, height = 70, posX = 750, posY = 600, text = "Quit").apply {
        visual = ColorVisual(Color(221,136,136))
        onMouseClicked = {
            app.exit()
        }
    }

    /*
    // Button for starting a new game
    val newGameButton = Button(width = 200, height = 70, posX = 1050, posY = 600, text = "New Game").apply {
        visual = ColorVisual(Color(136, 221, 136))
    }

     */

    init {
        background = ColorVisual(Color(143, 188, 143, 240))
        addComponents(headlineLabel, firstRank, firstScore, firstName, secondRank, secondName, secondScore,
            thirdRank, thirdName, thirdScore, fourthRank, fourthName, fourthScore,
            advancedScoring, quitButton)
    }

    override fun refreshAfterGameEnd() {

        val game = checkNotNull(rootService.currentGame)
        val state = game.gameStates[game.currentStateIndex]
        val players = game.gameStates[game.currentStateIndex].players
        val playerSize = game.gameStates[game.currentStateIndex].players.size

        // two player game
        if(playerSize == 2){
            thirdRank.isVisible = false
            thirdName.isVisible = false
            thirdScore.isVisible = false
            fourthName.isVisible = false
            fourthRank.isVisible = false
            fourthScore.isVisible = false
        }
        // three player game
        if(playerSize == 3){
            fourthName.isVisible = false
            fourthRank.isVisible = false
            fourthScore.isVisible = false
        }

        // Show advanced scoring label
        if(!state.advancedScoring){
            advancedScoring.isVisible = false
        }

        // Scores in Label
        firstScore.text = "${players[0].score}"
        secondScore.text = "${players[1].score}"

        // Names in Label
        firstName.text = players[0].name
        secondName.text = players[1].name

        //three player game
        if(playerSize == 3){
            thirdScore.text = "${players[2].score}"
            thirdName.text = players[2].name
        }

        //four player game
        if(playerSize == 4){
            thirdScore.text = "${players[2].score}"
            thirdName.text = players[2].name
            fourthScore.text = "${players[3].score}"
            fourthName.text = players[3].name
        }

        /*
        newGameButton.onMouseClicked = {
            if (rootService.networkService.connectionState != ConnectionState.DISCONNECTED) {
                // Scene will change in refresh in Application.
                rootService.networkService.disconnect()
            } else {
                //app.newGameMenuScene.updateButtons()
                app.showMenuScene(app.newGameMenuScene)
            }
        }

         */

    }
}