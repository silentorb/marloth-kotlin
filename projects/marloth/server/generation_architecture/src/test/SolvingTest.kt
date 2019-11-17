import generation.abstracted.newWindingWorkbench
import generation.architecture.definition.BlockDefinitions
import mythic.spatial.Vector3i
import org.junit.jupiter.api.Test
import randomly.Dice
import simulation.misc.Cell
import simulation.misc.MapGrid
import simulation.misc.NodeAttribute

val roomCell = Cell(attributes = setOf(NodeAttribute.fullFloor))

fun newCellStrip(cellPositions: List<Vector3i>) =
    MapGrid(
        cells = cellPositions.associateWith { roomCell },
        connections = (0 until cellPositions.size - 1)
            .map { i -> Pair(cellPositions[i], cellPositions[i + 1]) }
            .toSet()
    )

class SolvingTest {

  @Test
  fun matchesHorizontal() {
    val workbench = newWindingWorkbench(BlockDefinitions.singleCellRoom.block)
    val dice = Dice(1)
    val blocks = setOf(BlockDefinitions.singleCellRoom)
//    val block = matchBlock(dice, blocks, workbench, Vector3i(0, 1, 0))
//
//    Assertions.assertNotNull(block)
  }

//  @Test
//  fun matchesHorizontalHalfStep() {
//    val workbench = newWindingWorkbench(BlockDefinitions.halfStepRoom)
//    val dice = Dice(1)
//    val blocks = setOf(BlockDefinitions.halfStepRoom)
//    val block = matchConnectingBlock(dice, blocks, openConnectionTypes(), workbench,
//        Direction.west,
//        Vector3i(-1, 0, 0)
//    )
//    Assertions.assertNotNull(block)
//  }

}
