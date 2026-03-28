package entity

import kotlin.test.*

/**test for the [Block] class.*/


class BlockTest {

    /**
     * Tests that a Block is created with correct default values
     */
    @Test
    fun `Block is created correctly`() {
        val block = Block(Color.BLUE, BlockType.I2)

        assertEquals(Color.BLUE, block.color)
        assertEquals(BlockType.I2, block.blockType)
        assertEquals(Rotation.NONE, block.rotation)
        assertFalse(block.isMirrored)
    }

    /**
     * Tests that rotation can be changed
     */
    @Test
    fun blockRotation() {
        val block = Block(Color.RED, BlockType.O1)

        block.rotation = Rotation.NONE
        assertEquals(Rotation.NONE, block.rotation)

        block.rotation = Rotation.RIGHT
        assertEquals(Rotation.RIGHT, block.rotation)

        block.rotation = Rotation.BOTTOM
        assertEquals(Rotation.BOTTOM, block.rotation)

        block.rotation = Rotation.LEFT
        assertEquals(Rotation.LEFT, block.rotation)


    }

    /**
     * Tests that blocks with different colors and different Types can be created
     */
    @Test
    fun `Block can have different colors and blockTypes`() {
        val firstBlock = Block(Color.BLUE, BlockType.O1)
        val secondBlock = Block(Color.RED, BlockType.I2)
        val thirdBlock = Block(Color.GREEN, BlockType.V3)
        val fourthBlock = Block(Color.YELLOW, BlockType.I3)

        assertEquals(Color.BLUE, firstBlock.color)
        assertEquals(BlockType.O1, firstBlock.blockType)

        assertEquals(Color.RED, secondBlock.color)
        assertEquals(BlockType.I2, secondBlock.blockType)

        assertEquals(Color.GREEN, thirdBlock.color)
        assertEquals(BlockType.V3, thirdBlock.blockType)

        assertEquals(Color.YELLOW, fourthBlock.color)
        assertEquals(BlockType.I3, fourthBlock.blockType)

    }

    /**
     * [`all rows have same width`] tests if shape is indeed rectangular
     * */
    @Test
    fun `all rows have same width`() {
        for (type in BlockType.entries) {
            val block = Block(Color.GREEN, type)
            val width = block.tiles.first().size
            println(type)
            println(block.toString())
            assertTrue(block.tiles.all { it.size == width }, "Block $type is not rectangular")
        }
    }



}