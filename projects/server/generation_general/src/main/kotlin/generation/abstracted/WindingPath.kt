package generation.abstracted

import generation.general.*
import silentorb.mythic.debugging.conditionalDebugLog
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3i
import simulation.misc.BlockAttributes
import simulation.misc.CellAttribute
import simulation.misc.cellLength
import kotlin.math.pow

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
    if (block != null && block.attributes.contains(BlockAttributes.unique))
      GroupedBlocks(
          all = groupedBlocks.all - block,
          flexible = groupedBlocks.flexible - block,
      )
    else
      groupedBlocks

enum class BranchingMode {
  linear,
  open,
}

data class BlockState(
    val grid: BlockGrid,
    val blacklistSides: List<CellDirection>,
    val biomeBlocks: Map<String, GroupedBlocks>,
    val biomeGrid: BiomeGrid,
    val blacklistBlockLocations: Map<Vector3i, List<Block>>,
    val branchingMode: BranchingMode,
    val lastCell: Vector3i? = null,
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

fun getNextPosition(incompleteSide: CellDirection): Vector3i {
  val offset = directionVectors[incompleteSide.direction]!!
  val position = incompleteSide.cell
  return position + offset
}

fun getSignificantCellCount(grid: BlockGrid): Int =
    grid.entries
        .filter { it.value.offset == Vector3i.zero }
        .sumBy { it.value.source.significantCellCount }

fun getAvailableBlocks(groupedBlocks: GroupedBlocks, incompleteSides: List<CellDirection>, block: Block?): Set<Block> {
  val blocks = if (incompleteSides.size < 2)
    groupedBlocks.flexible
  else
    groupedBlocks.all

  return if (block != null && block.attributes.contains(BlockAttributes.hetero))
    blocks - block
  else
    blocks
}

tailrec fun addPathStep(
    maxSteps: Int,
    dice: Dice,
    state: BlockState
): BlockState {
  val (grid, blacklist) = state
  val incompleteSides = if (state.branchingMode == BranchingMode.linear && state.lastCell != null) {
    val sides = getIncompleteBlockSides(grid, state.lastCell) - blacklist
    if (sides.any())
      sides
    else
      getIncompleteBlockSides(grid) - blacklist
  } else
    getIncompleteBlockSides(grid) - blacklist

  if (incompleteSides.none())
    return state

  if (grid.size > 1000)
    throw Error("Infinite loop in world generation.")

  val stepCount = getSignificantCellCount(grid)
//  worldGenerationLog {
//    "Grid size: ${grid.size}, Traversable: $stepCount, Required: $required, Optional: $optional"
//  }
  return if (stepCount >= maxSteps)
    state
  else {
    val incompleteSide = dice.takeOne(incompleteSides)
    val nextPosition = getNextPosition(incompleteSide)
    assert(!grid.containsKey(nextPosition))
//    val side = currentBlock.sides[incompleteSide.direction]!!
//    worldGenerationLog {
//      "Side: ${currentBlock.name} ${side.mineOld}, ${incompleteSide.position} ${incompleteSide.direction}"
//    }
    val biome = state.biomeGrid(nextPosition)
    val groupedBlocks = state.biomeBlocks[biome]
    if (groupedBlocks == null)
      throw Error("Biome mismatch")

    val blocks = getAvailableBlocks(groupedBlocks, incompleteSides, grid[state.lastCell]?.source)

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
      if (!cellAdditions.containsKey(nextPosition))
        matchConnectingBlock(dice, groupedBlocks.all - blocks, grid, nextPosition)
      assert(cellAdditions.containsKey(nextPosition))
      assert(cellAdditions.any { it.value.offset == Vector3i.zero })
      assert(cellAdditions.none { grid.containsKey(it.key) })
      state.copy(
          biomeBlocks = state.biomeBlocks + (biome to filterUsedUniqueBlock(block, groupedBlocks)),
          grid = grid + cellAdditions,
          lastCell = nextPosition,
      )
    }
    addPathStep(maxSteps, dice, nextState)
  }
}

fun windingPath(seed: Long, dice: Dice, config: BlockConfig, length: Int, grid: BlockGrid): BlockGrid {
  val blocks = filterUsedUniqueBlocks(grid, config.blocks)
  val groupedBlocks = config.biomes
      .associateWith { biome ->
        newGroupedBlocks(blocks.filter { it.biomes.contains(biome) })
      }
      .filterValues { it.flexible.any() }

  val biomeAnchors = newBiomeAnchors(groupedBlocks.keys, dice,
      worldRadius = length.toFloat().pow(1f / 4f) * cellLength,
      biomeSize = 15f,
      minGap = 2f
  )
  val biomeGrid = biomeGridFromAnchors(biomeAnchors)
  val state = BlockState(
      grid = grid,
      biomeBlocks = groupedBlocks,
      blacklistSides = listOf(),
      blacklistBlockLocations = mapOf(),
      biomeGrid = biomeGrid,
      branchingMode = BranchingMode.linear
  )

  val (firstLength, secondLength) =
      if (getDebugBoolean("LINEAR_MAP"))
        length to 0
      else
        0 to length

  for (i in 0..10) {
    val intermediateState = addPathStep(firstLength, dice, state)
    val nextState = addPathStep(secondLength, dice, intermediateState.copy(branchingMode = BranchingMode.open))
    if (nextState.grid.size >= length)
      return nextState.grid
    else
      println("Failed to generate world with seed $seed")
  }
  throw Error("Reached maximum failed world generation attempts")
}
