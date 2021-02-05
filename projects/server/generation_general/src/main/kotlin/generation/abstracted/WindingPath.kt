package generation.abstracted

import generation.general.*
import silentorb.mythic.debugging.conditionalDebugLog
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3i
import simulation.misc.CellAttribute

data class GroupedBlocks(
    val all: Set<Block>,
    val expansive: Set<Block>,
    val flexible: Set<Block>
)

fun newGroupedBlocks(blocks: Set<Block>): GroupedBlocks {
  val polyominoes = blocks
      .filter { block ->
        block.sidesOld
            .any {
              it.value.connectionLogic == ConnectionLogic.required
            }
      }
      .toSet()

  val narrow = blocks
      .filter { block ->
        block.sidesOld
            .minus(verticalDirections)
            .containsValue(endpoint)
      }
      .toSet()

  val expansive = blocks - narrow

  return GroupedBlocks(
      all = blocks,
      expansive = expansive,
      flexible = expansive - polyominoes
  )
}

fun filterUsedUniqueBlock(block: Block?, groupedBlocks: GroupedBlocks): GroupedBlocks =
    if (block != null && block.attributes.contains(CellAttribute.unique))
      GroupedBlocks(
          all = groupedBlocks.all - block,
          expansive = groupedBlocks.expansive - block,
          flexible = groupedBlocks.flexible - block
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
    state: BlockState,
    bookMark: Triple<Vector3i, Block, BlockState>? = null
): BlockGrid {
  val (groupedBlocks, grid, blacklist) = state
  val incompleteSides = getIncompleteBlockSides(grid) - blacklist
  if (incompleteSides.none())
    return grid

  if (grid.size > 1000)
    throw Error("Infinite loop in world generation.")

//  val sideGroups = incompleteSides.groupBy {
//    grid[it.position]!!.sides[it.direction]!!.connectionLogic
//  }
//  val prioritySides = sideGroups[ConnectionLogic.required] ?: listOf()

  val stepCount = grid.count { it.value.cell.attributes.contains(CellAttribute.isTraversable) }
//  worldGenerationLog {
//    val required = sideGroups.getOrElse(ConnectionLogic.required) { listOf() }.size
//    val optional = sideGroups.getOrElse(ConnectionLogic.optional) { listOf() }.size
//    "Grid size: ${grid.size}, Traversable: $stepCount, Required: $required, Optional: $optional"
//  }
  return if (stepCount >= maxSteps)
    grid
  else {
    val incompleteSide = dice.takeOne(incompleteSides)

//    else if (sideGroups.containsKey(ConnectionLogic.optional))
//      dice.takeOne(sideGroups[ConnectionLogic.optional]!!)
//    else
//      dice.takeOne(incompleteSides)

    val offset = directionVectors[incompleteSide.direction]!!
    val position = incompleteSide.position
    val nextPosition = position + offset
    assert(!grid.containsKey(nextPosition))
    val currentBlock = grid[position]!!.cell
    val side = currentBlock.sides[incompleteSide.direction]!!
//    worldGenerationLog {
//      "Side: ${currentBlock.name} ${side.mineOld}, ${incompleteSide.position} ${incompleteSide.direction}"
//    }
    val blocks = if (incompleteSides.size < 2)
      if (stepCount >= maxSteps)
        groupedBlocks.flexible
      else
        groupedBlocks.expansive
    else
      groupedBlocks.all

    if (nextPosition == Vector3i(1, 3, 0)) {
      val k = 0
    }
    val (blockOffset, block) = matchConnectingBlock(dice, blocks, grid, nextPosition)
//        ?: matchConnectingBlock(dice, groupedBlocks.all, grid, nextPosition)

    val nextState = if (block == null) {
      if (bookMark != null && side.connectionLogic == ConnectionLogic.required) {
        val (initialLocation, initialBlock, bookMarkState) = bookMark
        val previousBlocks = bookMarkState.blacklistBlockLocations[initialLocation] ?: listOf()
        val newBlackListEntry = initialLocation to previousBlocks + initialBlock
        worldGenerationLog { "Rolling back to $initialLocation ${initialBlock.name}" }
        bookMarkState.copy(
            blacklistBlockLocations = bookMarkState.blacklistBlockLocations + newBlackListEntry
        )
      } else {
        state.copy(
            blacklistSides = state.blacklistSides + incompleteSide
        )
      }
    } else {
      worldGenerationLog { "Block: ${block.name}" }
      val cellAdditions = extractCells(block, nextPosition)
      state.copy(
          groupedBlocks = filterUsedUniqueBlock(block, groupedBlocks),
          grid = grid + cellAdditions
      )
    }

    val nextBookmark = if (block == null)
      null // Consume bookmark
    else if (incompleteSides.none()) {
      if (block.sidesOld.any { (direction, side) ->
            side.connectionLogic == ConnectionLogic.required &&
                !grid.containsKey(nextPosition + directionVectors[direction]!!)
          })
        bookMark ?: Triple(nextPosition, block, state) // Place or persist bookmark
      else
        null // Remove finished bookmark
    } else
      bookMark // Persist bookmark

    addPathStep(maxSteps, dice, nextState, nextBookmark)
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
