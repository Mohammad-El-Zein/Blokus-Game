package gui.startMenuComponents

import entity.Color
import entity.GameMode
import gui.GameModeSelectorPane
import gui.NewGameMenuScene
import tools.aqua.bgw.animation.MovementAnimation
import tools.aqua.bgw.animation.ParallelAnimation
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.core.MenuScene

/**
 * The specific GameModeSelectorPane for 4 Players
 */
class FourPlayerSelectionPane(menuScene: NewGameMenuScene): GameModeSelectorPane(menuScene){
    override fun initializePlayerAreas() {
        playerInputs.add(PlayerSelectionArea(
            posX = 80, posY = 400, width = 1350, text = "Player 1 name", color = Color.BLUE, isColorOnly = false
        ))
        playerInputs.add(PlayerSelectionArea(
            posX = 80, posY = 570, width = 1350, text = "Player 2 name", color = Color.YELLOW, isColorOnly = false
        ))
        playerInputs.add(PlayerSelectionArea(
            posX = 80, posY = 740, width = 1350, text = "Player 3 name", color = Color.RED, isColorOnly = false
        ))
        playerInputs.add(PlayerSelectionArea(
            posX = 80, posY = 910, width = 1350, text = "Player 4 name", color = Color.GREEN, isColorOnly = false
        ))
        for (playerinput in playerInputs) {
            println("Color Button zu einem playerInput hinzugefügt")
            playerinput.currentComponentList.add(
                Button(
                    posX = playerinput.posX+1300,
                    posY = playerinput.posY + 5,
                    width = 50,
                    height = 70,
                    visual = playerinput.getUpArrowVisual(),
                ).apply {
                    onMouseClicked = {
                        val i = playerInputs.indexOf(playerinput)
                        if (i != 0) {
                            if (playerInputs[i].isComplete() && playerInputs[i - 1].isComplete()) {
                                playerInputs[i].swapContents(playerInputs[i-1],true)
                            }
                        }
                    }
                }
            )
            playerinput.currentComponentList.add(
                Button(
                    posX = playerinput.posX+1300,
                    posY = playerinput.posY + 80,
                    width = 50,
                    height = 70,
                    visual = playerinput.getDownArrowVisual()
                ).apply {
                    onMouseClicked = {
                        val i = playerInputs.indexOf(playerinput)
                        if(i != playerInputs.size-1){
                            if (playerInputs[i].isComplete() && playerInputs[i+1].isComplete()){
                                playerInputs[i].swapContents(playerInputs[i+1],true)
                            }
                        }
                    }
                }
            )
        }
    }

    override fun enableHostingConfigurationOptions() {
        for(i in 0..<playerNum(menuScene.currentGameMode)){
            playerInputs[i].currentComponentList[playerInputs[i].currentComponentList.size-1].isDisabled = false
            playerInputs[i].currentComponentList[playerInputs[i].currentComponentList.size-1].isVisible = true
            playerInputs[i].currentComponentList[playerInputs[i].currentComponentList.size-2].isDisabled = false
            playerInputs[i].currentComponentList[playerInputs[i].currentComponentList.size-2].isVisible = true
        }
        super.enableHostingConfigurationOptions()
    }
}