package generation.abstracted

import generation.general.*
import silentorb.mythic.debugging.conditionalDebugLog
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3i
import simulation.misc.CellAttribute

data class GroupedBlocks(
    val all: Set<Block>,
    val flexible: Set<Block>,
)

fun newGroupedBlocks(blocks: Set<Block>): GroupedBlocks {
  val flexible = blocks
      .filter { block ->
        val traversableCount = block.cells.values
            .sumBy { cell ->
              cell.sides.count {
                it.value.isTraversable
              }
            }
        traversableCount >= 4
      }
      .toSet()

  return GroupedBlocks(
      all = blocks,
      flexible = flexible,
  )
}

fun filterUsedUniqueBlock(block: Block?, groupedBlocks: GroupedBlocks): GroupedBlocks =
    if (block != null && block.attributes.contains(CellAttribute.unique))
      GroupedBlocks(
          all = groupedBlocks.all - block,
          flexible = groupedBlocks.flexible - block,
      )
    else
      groupedBlocks

data class BlockState(
    val groupedBlocks: GroupedBlocks,
    val grid: BlockGrid,
    val blacklistSides: List<AbsoluteSide>,
    val blacklistBlockLocations: Map<Vector3i, List<Block>>
)

const val debugWorldGenerationKey = "DEBUG_WORLD_GENERATION"
val worldGenerationLog = conditionalDebugLog(debugWorldGenerationKey)

fun extractCells(block: Block, position: Vector3i) =
    block.cells.entries
        .associate { (cellOffset, cell) ->
          position + cellOffset to GridCell(
              cell = cell,
              offset = cellOffset,
              source = block,
          )
        }

tailrec fun addPathStep(
    maxSteps: Int,
    dice: Dice,
    state: BlockState
): BlockGrid {
  val (groupedBlocks, grid, blacklist) = state
  val incompleteSides = getIncompleteBlockSides(grid) - blacklist
  if (incompleteSides.none())
    return grid

  if (grid.size > 1000)
    throw Error("Infinite loop in world generation.")

  val stepCount = grid.count { it.value.cell.isTraversable }
//  worldGenerationLog {
//    "Grid size: ${grid.size}, Traversable: $stepCount, Required: $required, Optional: $optional"
//  }
  return if (stepCount >= maxSteps)
    grid
  else {
    val incompleteSide = dice.takeOne(incompleteSides)

    val offset = directionVectors[incompleteSide.direction]!!
    val position = incompleteSide.position
    val nextPosition = position + offset
    assert(!grid.containsKey(nextPosition))
    val currentBlock = grid[position]!!.cell
//    val side = currentBlock.sides[incompleteSide.direction]!!
//    worldGenerationLog {
//      "Side: ${currentBlock.name} ${side.mineOld}, ${incompleteSide.position} ${incompleteSide.direction}"
//    }
    val blocks = if (incompleteSides.size < 2)
      groupedBlocks.flexible
    else
      groupedBlocks.all

    val matchResult = matchConnectingBlock(dice, blocks, grid, nextPosition)
        ?: matchConnectingBlock(dice, groupedBlocks.all - blocks, grid, nextPosition)

    val nextState = if (matchResult == null) {
        state.copy(
            blacklistSides = state.blacklistSides + incompleteSide
        )
   } else {
      val (blockOffset, block) = matchResult
      worldGenerationLog { "Block: ${block.name}" }
      val cellAdditions = extractCells(block, nextPosition - blockOffset)
      state.copy(
          groupedBlocks = filterUsedUniqueBlock(block, groupedBlocks),
          grid = grid + cellAdditions
      )
    }

    addPathStep(maxSteps, dice, nextState)
  }
}

fun windingPath(dice: Dice, config: BlockConfig, length: Int): (BlockGrid) -> BlockGrid = { grid ->
  val blocks = filterUsedUniqueBlocks(grid, config.blocks)
  val groupedBlocks = newGroupedBlocks(blocks)
  val state = BlockState(groupedBlocks, grid, listOf(), mapOf())
  val nextGrid = addPathStep(length, dice, state)
  assert(nextGrid.size >= length)
  nextGrid
}
