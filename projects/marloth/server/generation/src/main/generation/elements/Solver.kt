package generation.elements

import mythic.spatial.Vector3i
import simulation.misc.MapGrid
import simulation.misc.containsConnection

data class AppliedPolyomino(
    val polyomino: Polyomino,
    val position: Vector3i
)

enum class InitialConnectionType {
  wall,
  connection
}

typealias IntermediateBlock = List<InitialConnectionType>

private fun checkPolyominoMatch(originalBlocks: Map<Vector3i, IntermediateBlock>,
                                newBlocks: Map<Vector3i, Block>): (Polyomino) -> Boolean = { polyomino ->
  true
}

private tailrec fun fillCellsIteration(remainingCells: Set<Vector3i>,
                                       polyominoes: List<Polyomino>,
                                       originalBlocks: Map<Vector3i, IntermediateBlock>,
                                       newBlocks: Map<Vector3i, Block>,
                                       accumulator: List<AppliedPolyomino>): List<AppliedPolyomino> {
  return if (remainingCells.any()) {
    val anchorCell = remainingCells.first()
    val match = polyominoes.firstOrNull(checkPolyominoMatch(originalBlocks, newBlocks))
    if (match == null)
      throw Error("Could not find a matching polyomino for the current grid configuration." +
          "  This is usually caused by not providing enough atomic, general polyominoes.")

    val applied = AppliedPolyomino(
        polyomino = match,
        position = anchorCell
    )
    fillCellsIteration(
        remainingCells.minus(anchorCell),
        polyominoes, originalBlocks, newBlocks,
        accumulator.plus(applied))
  } else
    accumulator
}

private fun mapGridToBlocks(grid: MapGrid): Map<Vector3i, IntermediateBlock> {
  return grid.cells.keys.associateWith { position ->
    val sides = sideDirections.values.map { offset ->
      if (containsConnection(grid.connections, position, position + offset))
        InitialConnectionType.connection
      else
        InitialConnectionType.wall
    }
    sides
  }
}

fun convertGridToElements(grid: MapGrid, polyominoes: List<Polyomino>): List<AppliedPolyomino> {
  assert(polyominoes.any())
  val blocks = mapGridToBlocks(grid)
  val remainingCells = grid.cells.keys
  return fillCellsIteration(remainingCells, polyominoes, blocks, mapOf(), listOf())
}
