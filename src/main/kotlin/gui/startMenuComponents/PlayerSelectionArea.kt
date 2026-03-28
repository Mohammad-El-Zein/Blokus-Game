package gui.startMenuComponents

import entity.Color
import entity.PlayerType
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.components.uicomponents.UIComponent
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.ImageVisual
import kotlin.toString

/**
 * PlayerSelectionArea manages all components that are associated with a single player and stores
 * those attributes. This includes the [entity.Player.name], the [entity.PlayerType] and the color(s) which the
 * player will play
 */
class PlayerSelectionArea(
    posX:Int,
    posY:Int,
    width: Int = 1300,
    text:String, color: Color,
    var isColorOnly: Boolean
): Pane<UIComponent>(
    posX,posY, width, height = 150){
    /**
     * originalColor stores the color with which this component had been initialized. This is so after swapping the
     * order in some gameModes the order is correct after switching to different gameModes
     */
    val originalColor = color
    var currentColor = color
    /**
     * The TextField where the name can be written in and is stored
     */
    val switchUp = Button(
        posX = posX + 0,
        posY = posY + 5,
        width = 90,
        height = 70,
        text = "up",
    )
    val switchDown = Button(
        posX = posX + 0,
        posY = posY + 80,
        width = 90,
        height = 70,
        text = "down"
    )
    val playerNameInput = TextField(
        posX = posX + 100,
        posY = posY + 0,
        width = 600,
        height = 150,
        prompt = text,
        font = Font(50)
    )

    /**
     * The custom ComboBox where the playerType is selected
     */
    val playerTypeSelection = PlayerTypeSelectionBox(
        posX = posX + 700,
        posY = posY + 0,
        width = 300,
        height = 150,
        items = listOf(PlayerType.PLAYER, PlayerType.TESTBOT, PlayerType.STOCKFISHWANNABE),
        formatFunction = {enumType -> enumType.toString()}
    )

    /**
     * The Label which shows the Color(s) the player will play
     */
    val playerColor = Label(
        posX = posX+1000,
        posY = posY + 0,
        width = 300,
        height = 150,
        visual = colorToColor(color)
    )

    /**
     * adds all components but the playerTypeSelection because it can extend outside the bounds of
     * this component. Have to add to main scene
     */
    val currentComponentList =
        mutableListOf(switchDown, switchUp, playerColor, playerNameInput,playerTypeSelection)

    init {
        if(isColorOnly){
            switchDown.isVisible = false
            switchUp.isVisible = false
            playerNameInput.isVisible = false
            playerTypeSelection.isVisible = false

            switchDown.isDisabled = true
            switchUp.isDisabled = true
            playerNameInput.isDisabled = true
            playerTypeSelection.isDisabled = true
        }
    }
    /**
     * Maps a [Color] to a [tools.aqua.bgw.visual.ColorVisual]
     *
     * @param color the [Color] instance
     *
     * @return the [tools.aqua.bgw.visual.ColorVisual] with the corresponding Color
     */
    fun colorToColor(color: Color): ColorVisual {
        return when(color){
            Color.BLUE -> ColorVisual.BLUE
            Color.YELLOW -> ColorVisual.YELLOW
            Color.RED -> ColorVisual.RED
            Color.GREEN -> ColorVisual.GREEN
            else -> ColorVisual.BLACK
        }
    }
    /**
     * Resets all components to the original state. Is useful when changing gameMode
     */
    fun reset(){
        playerNameInput.text = ""
        playerNameInput.isVisible = true
        playerNameInput.isDisabled = false

        playerColor.visual = colorToColor(originalColor)
        currentColor = originalColor
        playerColor.isVisible = true
        playerColor.isDisabled = false

        playerTypeSelection.isVisible = true
        playerTypeSelection.isDisabled = false
        playerTypeSelection.updateSelected(0)

        switchDown.isVisible = true
        switchDown.isDisabled = false
        switchUp.isVisible = true
        switchUp.isDisabled = false
    }
    /**
     * Changes the displayed color to a 50/50 mix of the two parameter colors.
     * //TODO ColorVisual and generate dinamically
     * @param color1 the first color
     * @param color2 the second color
     */
    fun setColorToMixColor(color1: Color, color2: Color?){
        /*var colorBasis = ImageIO.read(URL("multipleColorMask.png"))

        var tmpColor = CompoundVisual(color1)
        var mask1 = ImageVisual("multipleColorPlayer2.png",300,150)
        var mask2 = ImageVisual("multipleColorPlayer2.png",300,150, rotation = 180)*/

        //TODO(Make nicer with dynamic color image editing. Code so that parameter are used)
        if(color2 == color1 && color1 == Color.ADJACENT) {
            print("Das sollte nicht passieren...")
        }
        if (originalColor == Color.BLUE) {
            playerColor.visual = ImageVisual("multipleColorPlayer1.png")
        }
        if (originalColor == Color.YELLOW) {
            playerColor.visual = ImageVisual("multipleColorPlayer2.png")
        }

    }

    /**
     * Swaps its contents with the other playerSelectionArea.
     * @param other the playerSelectionArea from which the contents are swapped
     * @param swapColor A flag if the color is swapped to differentiate swapping playerOrder
     * or colorOrder
     */
    fun swapContents(other: PlayerSelectionArea, swapColor: Boolean){

        val tmp1 = other.playerNameInput.text
        other.playerNameInput.text = playerNameInput.text
        playerNameInput.text = tmp1

        val tmp2 = other.playerTypeSelection.selected
        other.playerTypeSelection.updateSelected(playerTypeSelection.selected)
        playerTypeSelection.updateSelected(tmp2)

        //SwapColor and swapping Invisible are mutually exclusive
        if(!playerNameInput.isVisible || !other.playerNameInput.isVisible){
            isColorOnly = other.isColorOnly
            other.isColorOnly = !isColorOnly
            other.playerNameInput.isVisible = playerNameInput.isVisible
            other.playerNameInput.isDisabled = playerNameInput.isDisabled
            other.playerTypeSelection.isVisible = playerTypeSelection.isVisible
            other.playerTypeSelection.isDisabled = playerTypeSelection.isDisabled

            playerNameInput.isVisible = !other.playerNameInput.isVisible
            playerNameInput.isDisabled = !other.playerNameInput.isDisabled
            playerTypeSelection.isVisible = !other.playerTypeSelection.isVisible
            playerTypeSelection.isDisabled = !other.playerTypeSelection.isDisabled
        }

        if(swapColor){
            val tmp3 = other.currentColor
            println("tmp3: ${tmp3.toString()}")
            other.currentColor = currentColor
            currentColor = tmp3

            val tmp4 = other.playerColor.visual
            println("tmp4: ${tmp4.toString()}")
            other.playerColor.visual = playerColor.visual
            playerColor.visual = tmp4


            //The swapColorArrows

            val tmp5 = other.currentComponentList[other.currentComponentList.size-2].visual
            println("tmp5: ${tmp5.toString()}")
            other.currentComponentList[other.currentComponentList.size-2].visual =
                currentComponentList[currentComponentList.size-2].visual
            currentComponentList[currentComponentList.size-2].visual = tmp5

            //The swapColorArrows
            val tmp6 = other.currentComponentList[other.currentComponentList.size-1].visual
            println("tmp6: ${tmp6.toString()}")
            other.currentComponentList[other.currentComponentList.size-1].visual =
                currentComponentList[currentComponentList.size-1].visual
            currentComponentList[currentComponentList.size-1].visual = tmp6
        }
    }

    /**
     * creates a Visual of an arrow pointing up with the
     * color of this PlayerSelectionArea object
     *
     * @return the Visual of the Arrow
     */
    fun getUpArrowVisual(): CompoundVisual{
        return CompoundVisual(
            colorToColor(currentColor),
            ImageVisual("ArrowMaskUp.png")
        )
    }
    /**
     * creates an Visual of an arrow pointing down with the
     * color of this PlayerSelectionArea object
     *
     * @return the Visual of the Arrow
     */
    fun getDownArrowVisual(): CompoundVisual{
        return CompoundVisual(
            colorToColor(currentColor),
            ImageVisual("ArrowMaskDown.png")
        )
    }
    /**
     * Returns if the input fields have values and a player could be created
     * @return if the input fields have values and a player could be created
     */
    fun isComplete(): Boolean {
        return (playerNameInput.text.isNotBlank() || (!playerNameInput.isVisible && !playerTypeSelection.isVisible))
    }
}