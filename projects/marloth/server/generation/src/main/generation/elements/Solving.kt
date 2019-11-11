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

typealias GetSide = (Vector3i, Direction) -> Side?
typealias GetBlock = (Vector3i) -> Block?
typealias CheckBlockSide = (Map.Entry<Direction, Side>) -> Boolean

//fun getOtherSide(blocks: BlockMap): GetSide = { origin, side ->
//  val oppositeSide = oppositeSides[side]!!
//  val offset = allDirections[side]!!
//  val position = origin + offset
//  blocks[position]?.get(oppositeSide)
//}

fun getSelfSide(blocks: BlockGrid): GetSide = { origin, side ->
  blocks[origin]?.sides?.get(side)
}

fun sidesMatch(getBlock: GetBlock, origin: Vector3i): CheckBlockSide = { (direction, side) ->
  val otherSide = getBlock(origin)?.sides?.get(direction) ?: setOf()
  val result = (side.none() && otherSide.none()) || otherSide.any { side.contains(it) }
  result
}

fun checkBlockMatch(getBlock: GetBlock, position: Vector3i): (Block) -> Boolean = { block ->
  block.sides.all(sidesMatch(getBlock, position))
}

fun matchBlock(dice: Dice, blocks: Set<Block>, workbench: Workbench, position: Vector3i): Block? {
  val getBlock: GetBlock = { workbench.blockGrid[it] }
  val shuffledBlocks = dice.shuffle(blocks.toList())
  return shuffledBlocks.firstOrNull(checkBlockMatch(getBlock, position))
}

fun checkPolyominoMatch(getBlock: GetBlock, origin: Vector3i): (Polyomino) -> Boolean = { polyomino ->
  polyomino.all { (blockPosition, block) ->
    val position = origin + blockPosition
    block.sides.all(sidesMatch(getBlock, position))
  }
}

fun translatePolyominoBlocks(polyomino: Polyomino, offset: Vector3i): Polyomino =
    polyomino.entries.associate { (position, block) -> Pair(offset + position, block) }

private fun fillCellsIteration(dice: Dice,
                               remainingCells: Set<Vector3i>,
                               polyominoes: Set<Polyomino>,
                               blocks: BlockGrid,
                               accumulator: List<AppliedPolyomino>): List<AppliedPolyomino> {
  return if (remainingCells.none())
    accumulator
  else {
    val anchorCell = remainingCells.first()
    val getBlock: GetBlock = { blocks[it] }
    val shuffledPolyominoes = dice.shuffle(polyominoes.toList())
    val polyomino = shuffledPolyominoes.firstOrNull(checkPolyominoMatch(getBlock, anchorCell))
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

fun mapGridToBlocks(initialConnectionTypes: Map<ConnectionCategory, Side>, grid: MapGrid): BlockGrid {
  return grid.cells.keys.associateWith { position ->
    val sides = allDirections.mapValues { (_, offset) ->
      if (containsConnection(grid.connections, position, position + offset))
        initialConnectionTypes[ConnectionCategory.open]!!
      else
        initialConnectionTypes[ConnectionCategory.closed]!!
    }
    Block(sides = sides, attributes = setOf())
  }
}

fun convertGridToElements(dice: Dice, initialConnectionTypes: Map<ConnectionCategory, Side>,
                          grid: MapGrid, polyominoes: Set<Polyomino>): List<AppliedPolyomino> {
  assert(polyominoes.any())
  val blocks = mapGridToBlocks(initialConnectionTypes, grid)
  val remainingCells = grid.cells.keys
  return fillCellsIteration(dice, remainingCells, polyominoes, blocks, listOf())
}
