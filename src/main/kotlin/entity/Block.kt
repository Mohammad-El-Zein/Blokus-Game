package entity
import kotlinx.serialization.Serializable

/**
 * Represents a block object in the game.
 *
 * @constructor Creates a block with the given tiles and size.
 *
 * @param color the color of the block
 * @param blockType the type of the block
 *
 * @property color the color of the block
 * @property blockType the type of the block
 */
@Serializable
class Block(val color: Color, val blockType: BlockType) {
    /** the matrix that contains the layout of the block */
    var tiles: MutableList<MutableList<Color>> = mutableListOf()
    /** indicates to the NetworkLayer if the block is mirrored
     * may only be written to by mirror() and rotate() */
    var isMirrored: Boolean = false
    /** indicates to the NetworkLayer how far the block is rotated
     * may only be written to by mirror() and rotate() */
    var rotation: Rotation = Rotation.NONE

    init {
        if (tiles.isEmpty()) {
            when (blockType) {
                BlockType.I5 -> {
                    rectangleReuse(6)
                }

                BlockType.I4 -> {
                    rectangleReuse(5)
                }

                BlockType.I3 -> {
                    rectangleReuse(4)
                }

                BlockType.I2 -> {
                    rectangleReuse(3)
                }

                BlockType.O1 -> {
                    rectangleReuse(2)
                }

                BlockType.O4 -> {
                    rectangleReuse(3)
                    val middleRow = middleRowFilled(3)
                    tiles.add(tiles.size - 2, middleRow)
                }

                BlockType.L4 -> {
                    rectangleReuse(4)
                    tiles.removeFirst()
                    val scndRow = middleRowFilled(4)
                    scndRow.removeFirst(); scndRow.removeFirst()
                    scndRow.add(0, color)
                    scndRow.add(0, Color.ADJACENT)
                    tiles.add(0, scndRow)
                    val firstRow = mutableListOf(Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL)
                    fillWillBlanks(2, firstRow)
                    tiles = mutableListOf(
                        mutableListOf(Color.DIAGONAL,Color.ADJACENT,Color.DIAGONAL,Color.BLANK,Color.BLANK),
                        mutableListOf(Color.ADJACENT,color,Color.ADJACENT,Color.ADJACENT,Color.DIAGONAL),
                        mutableListOf(Color.ADJACENT,color,color,color,Color.ADJACENT),
                        mutableListOf(Color.DIAGONAL,Color.ADJACENT,Color.ADJACENT,Color.ADJACENT,Color.DIAGONAL)
                    )
                }

                BlockType.U5 -> {
                    rectangleReuse(4)
                    tiles.removeLast()
                    val row2 = mutableListOf(
                        Color.ADJACENT, color, Color.ADJACENT,
                        color, Color.ADJACENT
                    )
                    val row3 = mutableListOf(
                        Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL, Color.ADJACENT,
                        Color.DIAGONAL
                    )
                    tiles.add(row2)
                    tiles.add(row3)
                }

                BlockType.L5 -> {
                    rectangleReuse(5)
                    tiles.last()[0] = Color.ADJACENT; tiles.last()[1] = color
                    val row3 = mutableListOf(Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL)
                    fillWillBlanks(3, row3)
                    tiles.add(row3)
                }

                BlockType.T4 -> {
                    rectangleReuse(4)
                    tiles.first()[2] = color
                    val row = mutableListOf(Color.BLANK, Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL, Color.BLANK)
                    tiles.add(0, row)
                }

                BlockType.P5 -> {
                    rectangleReuse(3)
                    val r1 = mutableListOf(Color.ADJACENT, color, color, Color.ADJACENT)
                    tiles.add(1, r1)
                    tiles.last()[0] = Color.ADJACENT; tiles.last()[1] = color
                    val row = mutableListOf(Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL, Color.BLANK)
                    tiles.add(row)
                }

                BlockType.Y5 -> {
                    rectangleReuse(5)
                    tiles.last()[2] = color
                    val row = mutableListOf(Color.BLANK, Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL)
                    fillWillBlanks(2, row)
                    tiles.add(row)
                }

                BlockType.V3 -> {
                    rectangleReuse(3)
                    tiles.last()[2] = color
                    tiles.last()[3] = Color.ADJACENT
                    val row = mutableListOf(Color.BLANK, Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL)
                    tiles.add(row)
                }

                BlockType.X5 -> {
                    rectangleReuse(4)
                    tiles.first()[2] = color; tiles.last()[2] = color
                    val row = mutableListOf(Color.BLANK, Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL, Color.BLANK)
                    tiles.add(row)
                    tiles.add(0, row.toMutableList())
                }

                BlockType.F5 -> {
                    rectangleReuse(4)
                    tiles.first()[2] = color; tiles.last()[1] = color; tiles.last()[0] = Color.ADJACENT
                    val row = mutableListOf(Color.BLANK, Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL, Color.BLANK)
                    tiles.add(0, row)
                    val r = mutableListOf(Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL)
                    fillWillBlanks(2, r)
                    tiles.add(r)

                }

                BlockType.T5 -> {
                    rectangleReuse(4)
                    tiles.first()[2] = color
                    val row = mutableListOf(Color.BLANK, Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL, Color.BLANK)
                    tiles.add(0, row)
                    val r = mutableListOf(Color.BLANK, Color.ADJACENT, color, Color.ADJACENT, Color.BLANK)
                    tiles.add(1, r)
                }

                BlockType.V5 -> {
                    rectangleReuse(4)
                    tiles.first()[0] = Color.ADJACENT; tiles.first()[1] = color; tiles.first()[2] = Color.ADJACENT
                    val r = mutableListOf(Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL)
                    fillWillBlanks(2, r)
                    tiles.add(0, r)
                    val row = mutableListOf(Color.ADJACENT, color, Color.ADJACENT, Color.BLANK, Color.BLANK)
                    tiles.add(1, row)

                }

                BlockType.W5 -> {
                    val r0 = mutableListOf(Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL)
                    fillWillBlanks(2, r0)
                    val r1 = mutableListOf(Color.ADJACENT, color, Color.ADJACENT, Color.DIAGONAL, Color.BLANK)
                    rectangleReuse(4)
                    tiles.first()[0] = Color.ADJACENT; tiles.first()[1] = color; tiles.first()[2] = color
                    tiles[1][0] = Color.DIAGONAL; tiles[1][1] = Color.ADJACENT
                    tiles.last()[0] = Color.BLANK; tiles.last()[1] = Color.DIAGONAL
                    tiles.add(0, r0)
                    tiles.add(1, r1)
                }

                BlockType.Z4 -> {
                    val r0 = mutableListOf(Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL, Color.BLANK)
                    rectangleReuse(3)
                    tiles.first()[0] = Color.ADJACENT; tiles.first()[1] = color
                    tiles.last()[3] = Color.ADJACENT; tiles.last()[2] = color
                    val r4 = mutableListOf(Color.BLANK, Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL)
                    tiles.add(0, r0)
                    tiles.add(r4)
                }

                BlockType.Z5 -> {
                    val r0 = mutableListOf(Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL)
                    fillWillBlanks(2, r0)
                    rectangleReuse(4)
                    tiles.first()[0] = Color.ADJACENT; tiles.first()[1] = color
                    tiles.last()[3] = color; tiles.last()[4] = Color.ADJACENT
                    val r4 = mutableListOf(Color.BLANK, Color.BLANK, Color.DIAGONAL, Color.ADJACENT, Color.DIAGONAL)
                    tiles.add(0, r0)
                    tiles.add(r4)

                }

                BlockType.N5 -> {
                    rectangleReuse(5)
                    tiles.first()[0] = Color.BLANK; tiles.first()[1] = Color.DIAGONAL
                    tiles[1][0] = Color.DIAGONAL; tiles[1][1] = Color.ADJACENT
                    tiles.last()[0] = Color.ADJACENT; tiles.last()[1] = color; tiles.last()[2] = color
                    val r3 = mutableListOf(Color.DIAGONAL, Color.ADJACENT, Color.ADJACENT, Color.DIAGONAL)
                    fillWillBlanks(2, r3)
                    tiles.add(r3)
                }
            }
            val newTiles = MutableList(tiles[0].size) {
                MutableList(tiles.size) { Color.BLANK } // or any default value
            }
            for (i in tiles.indices) {
                for (j in tiles[i].indices) {
                    newTiles[j][i] = tiles[i][j]
                }
            }
            tiles = newTiles
        }
    }

    private fun fillWillBlanks(qnt: Int, toFill: MutableList<Color>) {
        for (i in 0 until qnt) {
            toFill.add(Color.BLANK)
        }
    }
    private fun middleRowFilled(y: Int): MutableList<Color>{
        val row: MutableList<Color> = mutableListOf()
        row.add(Color.ADJACENT)
        for (i in 1 until y){
            row.add(color)
        }
        row.add(Color.ADJACENT)
        return row
    }

    private fun rectangleReuse(y: Int){

        val row0 = mutableListOf<Color>()
        val row1 = mutableListOf<Color>()
        val row2 = mutableListOf<Color>()
        row0.add(Color.DIAGONAL)
        row1.add(Color.ADJACENT)
        row2.add(Color.DIAGONAL)
       for ( i in 1 until y) {
            row0.add(i, Color.ADJACENT)
            row1.add(color)
            row2.add(Color.ADJACENT)
        }
        row0.add(Color.DIAGONAL)
        row1.add(Color.ADJACENT)
        row2.add(Color.DIAGONAL)

        tiles.add(row0); tiles.add(row1);tiles.add(row2)
    }

    override fun toString(): String{
        var str=""
        tiles.forEach { row ->
            row.forEach { color ->
                str += when(color){
                    Color.CORNER -> "C"
                    Color.DIAGONAL -> "D"
                    Color.ADJACENT -> "A"
                    Color.YELLOW -> "Y"
                    Color.BLANK -> "-"
                    Color.GREEN -> "G"
                    Color.BLUE -> "B"
                    Color.RED -> "R"
                    Color.BLOCKED -> "X"
                }
            }
            str+="\n"
        }
        return str
    }


}


