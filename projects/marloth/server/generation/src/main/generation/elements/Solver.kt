package generation.elements

import mythic.spatial.Vector3i
import simulation.misc.MapGrid
import simulation.misc.containsConnection

data class AppliedPolyomino(
    val polyomino: Polyomino,
    val position: Vector3i
)

enum class ConnectionCategory {
  closed,
  open
}

private typealias BlockMap = Map<Vector3i, Block>
private typealias GetSide = (Vector3i, Direction) -> Side?
private typealias CheckBlockSide = (Map.Entry<Direction, Side>) -> Boolean

private fun getOtherSide(blocks: BlockMap): GetSide = { origin, side ->
  val oppositeSide = oppositeSides[side]!!
  val offset = sideDirections[side]!!
  val position = origin + offset
  blocks[position]?.get(oppositeSide)
}

private fun sidesMatch(getSide: GetSide, origin: Vector3i): CheckBlockSide = { (direction, side) ->
  val otherSide = getSide(origin, direction)
  otherSide == null || otherSide.any { side.contains(it) }
}

private fun checkPolyominoMatch(getSide: GetSide, origin: Vector3i): (Polyomino) -> Boolean = { polyomino ->
  polyomino.all { (blockPosition, block) ->
    val position = origin + blockPosition
    block.all(sidesMatch(getSide, position))
  }
}

fun translatePolyominoBlocks(polyomino: Polyomino, offset: Vector3i): Polyomino =
    polyomino.entries.associate { (position, block) -> Pair(offset + position, block) }

private tailrec fun fillCellsIteration(remainingCells: Set<Vector3i>,
                                       polyominoes: Set<Polyomino>,
                                       blocks: BlockMap,
                                       accumulator: List<AppliedPolyomino>): List<AppliedPolyomino> {
  return if (remainingCells.none())
    accumulator
  else {
    val anchorCell = remainingCells.first()
    val getSide = getOtherSide(blocks)
    val polyomino = polyominoes.firstOrNull(checkPolyominoMatch(getSide, anchorCell))
    if (polyomino == null)
      throw Error("Could not find a matching polyomino for the current grid configuration." +
          "  This is usually caused by not providing enough atomic, general polyominoes.")

    val applied = AppliedPolyomino(
        polyomino = polyomino,
        position = anchorCell
    )
    val newBlocks = blocks.plus(translatePolyominoBlocks(polyomino, anchorCell))
    fillCellsIteration(remainingCells.minus(anchorCell), polyominoes, newBlocks, accumulator.plus(applied))
  }
}

private fun mapGridToBlocks(initialConnectionTypes: Map<ConnectionCategory, Side>, grid: MapGrid): BlockMap {
  return grid.cells.keys.associateWith { position ->
    val sides = sideDirections.mapValues { (_, offset) ->
      if (containsConnection(grid.connections, position, position + offset))
        initialConnectionTypes[ConnectionCategory.open]!!
      else
        initialConnectionTypes[ConnectionCategory.closed]!!
    }
    sides
  }
}

fun convertGridToElements(initialConnectionTypes: Map<ConnectionCategory, Side>,
                          grid: MapGrid, polyominoes: Set<Polyomino>): List<AppliedPolyomino> {
  assert(polyominoes.any())
  val blocks = mapGridToBlocks(initialConnectionTypes, grid)
  val remainingCells = grid.cells.keys
  return fillCellsIteration(remainingCells, polyominoes, blocks, listOf())
}
