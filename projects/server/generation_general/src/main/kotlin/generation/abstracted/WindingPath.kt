package generation.abstracted

import generation.general.*
import silentorb.mythic.ent.firstNotNull
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.randomly.Dice
import simulation.misc.Cell
import simulation.misc.MapGrid
import simulation.misc.CellAttribute

tailrec fun addPathStep(
    maxSteps: Int,
    dice: Dice,
    blocks: Set<Block>,
    grid: BlockGrid
): BlockGrid =
    if (grid.size == maxSteps)
      grid
    else {
      val incompleteSides = getIncompleteBlockSides(grid)
      val incompleteSide = dice.takeOne(incompleteSides)
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

      addPathStep(maxSteps, dice, blocks, nextGrid)
    }

fun windingPath(dice: Dice, config: BlockConfig, length: Int): (BlockGrid) -> BlockGrid = { grid ->
  val nextGrid = addPathStep(length - 1, dice, config.blocks, grid)
  assert(nextGrid.size > grid.size)
  nextGrid
}
