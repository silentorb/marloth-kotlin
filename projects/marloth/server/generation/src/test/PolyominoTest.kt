import generation.architecture.definition.PolyominoeDefinitions
import generation.architecture.initialConnectionTypesMap
import generation.elements.checkPolyominoMatch
import generation.elements.getOtherSide
import generation.elements.mapGridToBlocks
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

  @Test
  fun matchingVertical() {
    val grid = newCellStrip(listOf(
        Vector3i(0, 0, 0),
        Vector3i(0, 0, 1),
        Vector3i(1, 0, 1)
    ))
    val blocks = mapGridToBlocks(initialConnectionTypesMap(), grid)
    val anchorCell = Vector3i()
    val getSide = getOtherSide(blocks)
    val polyomino = PolyominoeDefinitions.spiralStairs
    val result = checkPolyominoMatch(getSide, anchorCell)(polyomino)

    Assertions.assertTrue(result)
  }
}
