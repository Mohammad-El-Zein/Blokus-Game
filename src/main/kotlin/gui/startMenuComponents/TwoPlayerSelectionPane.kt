package gui.startMenuComponents

import entity.Color
import entity.GameMode
import gui.GameModeSelectorPane
import gui.NewGameMenuScene
import tools.aqua.bgw.core.MenuScene

/**
 * The specific GameModeSelectorPane for 2 Players
 */
class TwoPlayerSelectionPane(menuScene: NewGameMenuScene): GameModeSelectorPane(menuScene) {
    override fun initializePlayerAreas() {
        playerInputs.add(PlayerSelectionArea(
            posX = 80,
            posY = 400,
            text = "Player 1 name",
            color = Color.BLUE,
            isColorOnly = false
        ))
        playerInputs.add(PlayerSelectionArea(
            posX = 80,
            posY = 570,
            text = "Player 2 name",
            color =  Color.YELLOW,
            isColorOnly = false
        ))
    }
    /**
     * Shows all offlineComponents
     */
    override fun show(pMode: MenuType) {
        for (i in playerInputs.indices) {
            when(menuScene.currentGameMode){
                GameMode.TWO_PLAYER_SMALL -> {
                    playerInputs[i].playerColor.visual = playerInputs[i].colorToColor(playerInputs[i].originalColor)
                }
                GameMode.TWO_PLAYER_FOUR_COLOR -> {
                    playerInputs[i].setColorToMixColor(
                        playerInputs[i].originalColor,
                        null
                    )
                }
                else -> {
                }
            }
        }
        super.show(pMode)
    }
}