package generation.elements

import mythic.spatial.Vector3i
import randomly.Dice
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

typealias BlockMap = Map<Vector3i, Block>
typealias GetSide = (Vector3i, Direction) -> Side?
typealias CheckBlockSide = (Map.Entry<Direction, Side>) -> Boolean

fun getOtherSide(blocks: BlockMap): GetSide = { origin, side ->
  val oppositeSide = oppositeSides[side]!!
  val offset = sideDirections[side]!!
  val position = origin + offset
  blocks[position]?.get(oppositeSide)
}

fun getSelfSide(blocks: BlockMap): GetSide = { origin, side ->
  blocks[origin]?.get(side)
}

fun sidesMatch(getSide: GetSide, origin: Vector3i): CheckBlockSide = { (direction, side) ->
  val otherSide = getSide(origin, direction) ?: setOf()
  val result = (side.none() && otherSide.none()) || otherSide.any { side.contains(it) }
  result
}

fun checkPolyominoMatch(getSide: GetSide, origin: Vector3i): (Polyomino) -> Boolean = { polyomino ->
  polyomino.all { (blockPosition, block) ->
    val position = origin + blockPosition
    block.all(sidesMatch(getSide, position))
  }
}

fun translatePolyominoBlocks(polyomino: Polyomino, offset: Vector3i): Polyomino =
    polyomino.entries.associate { (position, block) -> Pair(offset + position, block) }

private fun fillCellsIteration(dice: Dice,
                                       remainingCells: Set<Vector3i>,
                                       polyominoes: Set<Polyomino>,
                                       blocks: BlockMap,
                                       accumulator: List<AppliedPolyomino>): List<AppliedPolyomino> {
  return if (remainingCells.none())
    accumulator
  else {
    val anchorCell = remainingCells.first()
    val getSide = getSelfSide(blocks)
    val shuffledPolyominoes = dice.scramble(polyominoes.toList())
    val polyomino = shuffledPolyominoes.firstOrNull(checkPolyominoMatch(getSide, anchorCell))
    if (polyomino == null) {
      if (System.getenv("LOG_POLYOMINOES_ON_ERROR") != null) {
        polyominoes.forEach(::logPolyomino)
      }
      throw Error("Could not find a matching polyomino for the current grid configuration." +
          "  This is usually caused by not providing enough atomic, general polyominoes.")
    }

    val applied = AppliedPolyomino(
        polyomino = polyomino,
        position = anchorCell
    )
    val newBlocks = blocks.plus(translatePolyominoBlocks(polyomino, anchorCell))
    fillCellsIteration(dice, remainingCells.minus(anchorCell), polyominoes, newBlocks, accumulator.plus(applied))
  }
}

fun mapGridToBlocks(initialConnectionTypes: Map<ConnectionCategory, Side>, grid: MapGrid): BlockMap {
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

fun convertGridToElements(dice: Dice, initialConnectionTypes: Map<ConnectionCategory, Side>,
                          grid: MapGrid, polyominoes: Set<Polyomino>): List<AppliedPolyomino> {
  assert(polyominoes.any())
  val blocks = mapGridToBlocks(initialConnectionTypes, grid)
  val remainingCells = grid.cells.keys
  return fillCellsIteration(dice, remainingCells, polyominoes, blocks, listOf())
}
