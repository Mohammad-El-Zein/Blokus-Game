package gui.startMenuComponents

import entity.Color
import gui.GameModeSelectorPane
import gui.NewGameMenuScene
import tools.aqua.bgw.core.MenuScene

/**
 * The specific GameModeSelectorPane for 3 Players
 */
class ThreePlayerSelectionPane (menuScene: NewGameMenuScene): GameModeSelectorPane(menuScene){
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
            color = Color.YELLOW,
            isColorOnly = false
        ))
        playerInputs.add(PlayerSelectionArea(
            posX = 80,
            posY = 740,
            text = "Player 3 name",
            color = Color.RED,
            isColorOnly = false
        ))
        playerInputs.add(PlayerSelectionArea(
            posX = 80,
            posY = 910,
            text = "Player 4 name",
            color = Color.GREEN,
            isColorOnly = true
        ))
        /*playerInputs.last().apply {
            playerNameInput.isVisible = false
            playerNameInput.isDisabled = true
            playerTypeSelection.isVisible = false
            playerTypeSelection.isDisabled = true
            switchDown.isVisible = false
            switchDown.isDisabled = true
            switchUp.isVisible = false
            switchUp.isDisabled = true
        }*/
    }
}