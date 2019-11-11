import generation.architecture.definition.PolyominoeDefinitions
import generation.architecture.initialConnectionTypesMap
import generation.elements.*
import mythic.spatial.Vector3i
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import simulation.misc.Cell
import simulation.misc.MapGrid
import simulation.misc.NodeAttribute

val roomCell = Cell(attributes = setOf(NodeAttribute.room))

fun newCellStrip(cellPositions: List<Vector3i>) =
    MapGrid(
        cells = cellPositions.associateWith { roomCell },
        connections = (0 until cellPositions.size - 1)
            .map { i -> Pair(cellPositions[i], cellPositions[i + 1]) }
            .toSet()
    )

class PolyominoTest {

//  @Test
//  fun matchesVerticalUp() {
//    val grid = newCellStrip(listOf(
//        Vector3i(0, 0, 0),
//        Vector3i(0, 0, 1),
//        Vector3i(1, 0, 1)
//    ))
//    val blocks = mapGridToBlocks(initialConnectionTypesMap(), grid)
//    val anchorCell = Vector3i()
//    val getSide = getOtherSide(blocks)
//    val polyomino = PolyominoeDefinitions.spiralStairsSingle
//    val result = checkPolyominoMatch(getSide, anchorCell)(polyomino)
//
//    Assertions.assertTrue(result)
//  }
//
//  @Test
//  fun matchesStraightThenVerticalUp() {
//    val grid = newCellStrip(listOf(
//        Vector3i(0, 1, 0),
//        Vector3i(0, 0, 0),
//        Vector3i(0, 0, 1),
//        Vector3i(1, 0, 1)
//    ))
//    val blocks = mapGridToBlocks(initialConnectionTypesMap(), grid)
//    val anchorCell = Vector3i()
//    val getSide = getOtherSide(blocks)
//    val polyomino = PolyominoeDefinitions.spiralStairsSingle
//    val result = checkPolyominoMatch(getSide, anchorCell)(polyomino)
//
//    Assertions.assertTrue(result)
//  }
//
//  @Test
//  fun matchesVerticalDown() {
//    val grid = newCellStrip(listOf(
//        Vector3i(0, 0, 0),
//        Vector3i(0, 0, -1),
//        Vector3i(1, 0, -1)
//    ))
//    val blocks = mapGridToBlocks(initialConnectionTypesMap(), grid)
//    val anchorCell = Vector3i()
//    val getSide = getOtherSide(blocks)
//    val polyomino = translatePolyomino(Vector3i(0, 0, -1))(PolyominoeDefinitions.spiralStairsSingle)
//    val result = checkPolyominoMatch(getSide, anchorCell)(polyomino)
//
//    Assertions.assertTrue(result)
//  }

  @Test
  fun matchesTwiceVerticalUp() {
    val grid = newCellStrip(listOf(
        Vector3i(0, 0, 0),
        Vector3i(0, 0, 1),
        Vector3i(0, 0, 2),
        Vector3i(1, 0, 2)
    ))
    val blocks = mapGridToBlocks(initialConnectionTypesMap(), grid)
    val anchorCell = Vector3i()
    val getSide = getSelfSide(blocks)
//    val check = { position: Vector3i ->
//      checkPolyominoMatch(getSide, position)
//    }
//    Assertions.assertTrue(check(Vector3i.zero)(PolyominoeDefinitions.spiralStairsBottom))
//    Assertions.assertTrue(check(Vector3i(0, 0, 1))(PolyominoeDefinitions.spiralStairsMiddle))
//    Assertions.assertTrue(check(Vector3i(0, 0, 2))(PolyominoeDefinitions.spiralStairsTop))
  }
}
