package gui.startMenuComponents

import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.visual.ColorVisual

/**
 * A button which cycles between a custom set of
 * selectable objects, representable as strings
 */
class PlayerTypeSelectionBox<T>(
    posX: Int = 0,
    posY: Int = 0,
    width: Int,
    height: Int,
    val items: List<T>,
    var selected: Int = 0,
    val formatFunction: ((T) -> String)) : Button(
        posX = posX,
        posY = posY,
        width = width,
        height = height,
        visual = ColorVisual(10,10,10,0.1)
        ){

    init {
        showSelected()
        onMouseClicked = {
            selected++
            if (selected == items.size) selected = 0
            showSelected()
        }
    }

    /**
     * Returns the currently selected object
     * @return the currently selected object
     */
    fun getSelected():T{
        return items[selected]
    }
    /**
     * Updates and displays the currently displayed object
     * @param i the index of the new selected object in this object's items list
     */
    fun updateSelected(i: Int){
        selected = i
        showSelected()
    }

    /**
     * Displays the currently selected object
     */
    fun showSelected(){
        this.text = formatFunction(items[selected])
    }

}