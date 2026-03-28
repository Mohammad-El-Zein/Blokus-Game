package gui

import entity.Block
import entity.BlockType
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.gamecomponentviews.GameComponentView
import tools.aqua.bgw.components.layoutviews.Pane
import tools.aqua.bgw.visual.Visual

/**
 * Models the Area in which a player has access to his Blocks
 */
class PlayerArea(
    posX:Int,
    posY:Int,
    width:Int,
    height: Int,
    visual: Visual
): Pane<BlokusBlock>(posX,posY,width,height,visual) {

    /**
     * A list of all currently added Blocks
     */
    val currentlyDisplayedBlokus = mutableListOf<BlokusBlock>()

    /**
     * Adds a block to this area. It finds the first position where it has enough space and displays it there.
     * Adds it to this component and to currentlyDisplayedBlokus
     *
     * @param block The [BlokusBlock] you want to place on the Board
     */
    fun addBlock(block: BlokusBlock){
        var x = 10
        var y = 10
        //TODO(Schönere Anordnung machen)
        outer@ for(j in 0..(height/20).toInt()){
            for (i in 0..(width/20).toInt()) {
                if (fits(block,20*i,20*j)) {
                    x = i*20
                    y = j*20
                    break@outer
                }
            }
        }
        block.posX = x.toDouble()
        block.posY = y.toDouble()
        block.isVisible = true
        block.isDisabled = false
        add(block)
        currentlyDisplayedBlokus.add(block)
    }

    /**
     * Checks if the given Block fits at the given coordinates. If it overlaps with other components or some of
     * it is out of bounds it returns false, else true
     *
     * @param block The [BlokusBlock] you want to place
     * @param x The x position of the upper left corner where the bock is supposed to be placed
     * @param y The y position of the upper left corner where the bock is supposed to be placed
     *
     * @return If the block can be placed at the give coordinates
     */
    private fun fits(block: BlokusBlock, x: Int, y: Int): Boolean{
        if(x + block.width >= width) return false
        if (y + block.height >= height) return false
        for (i in currentlyDisplayedBlokus.indices){
            if (overlap(currentlyDisplayedBlokus[i],block,x,y)){
                return false
            }
        }
        return true
    }
    /**
     * Checks if the given Block fits at the given coordinates would overlap with a specific other block
     *
     * @param oldBlock The [BlokusBlock] which is already placed on this component
     * @param newBlock The [BlokusBlock] which you want to place on this component at the given coordinates
     * @param x The x position of the upper left corner where the new bock is supposed to be placed
     * @param y The y position of the upper left corner where the new bock is supposed to be placed
     *
     * @return If the blocks overlap which each other
     */
    private fun overlap(oldBlock: BlokusBlock, newBlock: BlokusBlock, newPosX: Int, newPosY: Int): Boolean {
        if(oldBlock.posX + oldBlock.width < newPosX) {
            return false
        }
        if(oldBlock.posY + oldBlock.height < newPosY) {
            return false
        }
        if(oldBlock.posX > newBlock.width+newPosX) {
            return false
        }
        if(oldBlock.posY > newBlock.height+newPosY) {
            return false
        }
        return true
    }

    /**
     * Removes a block from this component and from the list currentlyDisplayedBlokus
     * It finds the block corresponding to the given [entity.BlockType]
     * @param blockType The [entity.BlockType] of the [BlokusBlock] you want to remove
     */
    fun removeBlock(blockType: BlockType){
        val block = currentlyDisplayedBlokus.find { element -> element.blockType == blockType }
        if(block != null) {
            remove(block)
            currentlyDisplayedBlokus.remove(block)
        }
    }

    /**
     * Deletes all Components from this area and from the list currentlyDisplayedBlokus
     */
    fun reset(){
        currentlyDisplayedBlokus.clear()
        removeAll { true }
    }
}