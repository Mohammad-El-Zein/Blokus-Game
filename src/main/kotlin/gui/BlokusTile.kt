package gui

import entity.Color
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.Visual

/**
 * Models one sub square of playing pieces and the gameBoard.
 * //TODO(Change getting the right ImageVisual vor a Color from the callers side to here)
 */
class BlokusTile(posX: Int=0, posY: Int=0, width: Int, height: Int = width, var color: Color, visual: ImageVisual):
    TokenView(
        posX = posX,
        posY = posY,
        width = width,
        height = height,
        visual = visual){
    /**
     * Creates a copy of this tile. You can specify an offset which will add to this tiles position
     *
     * @param xOffset the x-offset
     * @param yOffset the y-offset
     * @return a copy of this Tile
     */
    fun copy(xOffset: Int, yOffset: Int): BlokusTile {
        return BlokusTile(
            posX = posX.toInt()+xOffset,
            posY = posY.toInt()+yOffset,
            width = width.toInt() ,
            height = height.toInt(),
            color = color,
            visual = visual.copy() as ImageVisual)
    }
}