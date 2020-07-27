package generation.abstracted

import generation.general.*
import silentorb.mythic.randomly.Dice
import simulation.misc.CellAttribute

tailrec fun addPathStep(
    maxSteps: Int,
    dice: Dice,
    blocks: Set<Block>,
    grid: BlockGrid,
    blacklist: List<AbsoluteSide>
): BlockGrid {
  val incompleteSides = getIncompleteBlockSides(grid) - blacklist
  if (incompleteSides.none())
    return grid

  val sideGroups = incompleteSides.groupBy {
    grid[it.position]!!.sides[it.direction]!!.connectionLogic
  }
  val prioritySides = sideGroups[ConnectionLogic.connectWhenPossible] ?: listOf()

  return if (grid.size >= maxSteps && prioritySides.none())
    grid
  else {
    val incompleteSide = if (prioritySides.any())
      dice.takeOne(prioritySides)
    else if (sideGroups.containsKey(ConnectionLogic.neutral))
      dice.takeOne(sideGroups[ConnectionLogic.neutral]!!)
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

    addPathStep(maxSteps, dice, filterUsedUniqueBlocks(nextGrid, blocks), nextGrid, nextBlacklist)
  }
}

fun windingPath(dice: Dice, config: BlockConfig, length: Int): (BlockGrid) -> BlockGrid = { grid ->
  val blocks = filterUsedUniqueBlocks(grid, config.blocks)
  val nextGrid = addPathStep(length, dice, blocks, grid, listOf())
  assert(nextGrid.size > grid.size)
  nextGrid
}
