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

fun newGroupedBlocks(blocks: Collection<Block>): GroupedBlocks {
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
      all = blocks.toSet(),
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
    val grid: BlockGrid,
    val blacklistSides: List<AbsoluteSide>,
    val biomeBlocks: Map<String, GroupedBlocks>,
    val biomeGrid: BiomeGrid,
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
  val (grid, blacklist) = state
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
    val biome = state.biomeGrid(nextPosition)
    val groupedBlocks = state.biomeBlocks[biome]
    if (groupedBlocks == null)
      throw Error("Biome mismatch")

    val blocks = if (incompleteSides.size < 2)
      groupedBlocks.flexible
    else
      groupedBlocks.all

    val matchResult = matchConnectingBlock(dice, blocks, grid, nextPosition)
        ?: matchConnectingBlock(dice, groupedBlocks.all - blocks, grid, nextPosition)
        ?: fallbackBiomeMatchConnectingBlock(dice, state.biomeBlocks, grid, nextPosition, biome)

    val nextState = if (matchResult == null) {
      state.copy(
          blacklistSides = state.blacklistSides + incompleteSide
      )
    } else {
      val (blockOffset, block) = matchResult
      worldGenerationLog { "Block: ${block.name}" }
      val cellAdditions = extractCells(block, nextPosition - blockOffset)
      state.copy(
          biomeBlocks = state.biomeBlocks + (biome to filterUsedUniqueBlock(block, groupedBlocks)),
          grid = grid + cellAdditions
      )
    }

    addPathStep(maxSteps, dice, nextState)
  }
}

fun windingPath(seed: Long, dice: Dice, config: BlockConfig, length: Int, grid: BlockGrid): BlockGrid {
  val blocks = filterUsedUniqueBlocks(grid, config.blocks)
  val groupedBlocks = config.biomes
      .associateWith { biome ->
        newGroupedBlocks(blocks.filter { it.biome == biome })
      }
      .filterValues { it.flexible.any() }

  val biomeAnchors = newBiomeAnchors(groupedBlocks.keys, dice, length)
  val biomeGrid = biomeGridFromAnchors(biomeAnchors)
  val state = BlockState(
      grid = grid,
      biomeBlocks = groupedBlocks,
      blacklistSides = listOf(),
      blacklistBlockLocations = mapOf(),
      biomeGrid = biomeGrid,
  )
  for (i in 0..10) {
    val nextGrid = addPathStep(length, dice, state)
    if (nextGrid.size >= length)
      return nextGrid
    else
      println("Failed to generate world with seed $seed")
  }
  throw Error("Reached maximum failed world generation attempts")
}
