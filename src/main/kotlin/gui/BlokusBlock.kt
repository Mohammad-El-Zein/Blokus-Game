package gui

import entity.BlockType
import entity.Color
import tools.aqua.bgw.components.container.Area

/**
 * Models one playing piece as a collection of [BlokusTile]. They are stored in an area
 */
class BlokusBlock(val blockType: BlockType, val color: Color, width:Int, height: Int):
    Area<BlokusTile>(width = width, height = height) {
    //not empty
}