package gui

import service.Refreshable
import tools.aqua.bgw.animation.Animation
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.animation.RotationAnimation
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ImageVisual
import java.time.Duration

/**
 * The Scene a player sees when he joins an online game until the host starts the game
 */
class WaitingScene : MenuScene(1920,1080), Refreshable {
    /**
     * The waiting text
     */
    val waitingLabel = Label(
        posX = 0,
        posY = 0,
        width = 1920,
        height = 780,
        text = "Waiting for Host to start...",
        font = Font(size = 100)
    )

    /**
     * The spinning waiting ball
     */
    val spinningCircle = Label(
        posX = 880,
        posY = 620,
        width = 200,
        height = 200,
        visual = ImageVisual("LoadingButton.png",200,200,0,0)
    )

    init {
        addComponents(waitingLabel,spinningCircle)
        //rotateLoadingCircle()
    }

    /**
     * Rotates the rotation ball [spinningCircle] every 50 ms by 10 degrees
     */
    private fun rotateLoadingCircle(){
        spinningCircle.rotate(10)
        playAnimation(DelayAnimation(duration = 50).apply {
            onFinished = {
                rotateLoadingCircle()
            }
        })
    }
}