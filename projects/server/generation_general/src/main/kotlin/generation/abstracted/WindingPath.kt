package generation.abstracted

import generation.general.*
import silentorb.mythic.randomly.Dice

tailrec fun addPathStep(
    maxSteps: Int,
    dice: Dice,
    blocks: Set<Block>,
    grid: BlockGrid,
    blacklist: List<AbsoluteSide>
): BlockGrid {
  val incompleteSides = getIncompleteBlockSides(grid) - blacklist
  val prioritySides = incompleteSides.filter {
    grid[it.position]!!.sides[it.direction]!!.closeIfPossible
  }

  return if (grid.size >= maxSteps && prioritySides.none())
    grid
  else {
    val incompleteSide = if (prioritySides.any())
      dice.takeOne(prioritySides)
    else
      dice.takeOne(incompleteSides)
    val offset = directionVectors[incompleteSide.direction]!!
    val position = incompleteSide.position
    val nextPosition = position + offset
    assert(!grid.containsKey(nextPosition))

    val block = matchConnectingBlock(dice, blocks, grid, nextPosition)

    val nextGrid = if (block == null) {
      grid
    } else {
      grid + (nextPosition to block)
    }

    val nextBlacklist = if (block == null) {
      blacklist + incompleteSide
    } else {
      blacklist
    }

    addPathStep(maxSteps, dice, blocks, nextGrid, nextBlacklist)
  }
}

fun windingPath(dice: Dice, config: BlockConfig, length: Int): (BlockGrid) -> BlockGrid = { grid ->
  val nextGrid = addPathStep(length, dice, config.blocks, grid, listOf())
  assert(nextGrid.size > grid.size)
  nextGrid
}
