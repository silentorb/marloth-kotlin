package generation.abstracted

import generation.general.*
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
        block.sides
            .any {
              it.value.connectionLogic == ConnectionLogic.required
            }
      }
      .toSet()

  val narrow = blocks
      .filter { block ->
        block.sides
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

  val sideGroups = incompleteSides.groupBy {
    grid[it.position]!!.sides[it.direction]!!.connectionLogic
  }
  val prioritySides = sideGroups[ConnectionLogic.required] ?: listOf()

  val stepCount = grid.count { it.value.attributes.contains(CellAttribute.traversable) }
  return if (stepCount >= maxSteps && prioritySides.none())
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
    val block = matchConnectingBlock(dice, blocks, grid, nextPosition)
        ?: matchConnectingBlock(dice, groupedBlocks.all, grid, nextPosition)

    val nextState = if (block == null) {
      if (bookMark != null) {
        val (initialLocation, initialBlock, bookMarkState) = bookMark
        val previousBlocks = bookMarkState.blacklistBlockLocations[initialLocation] ?: listOf()
        val newBlackListEntry = initialLocation to previousBlocks + initialBlock
        bookMarkState.copy(
            blacklistBlockLocations = bookMarkState.blacklistBlockLocations + newBlackListEntry
        )
      } else {
        state.copy(
            blacklistSides = state.blacklistSides + incompleteSide
        )
      }
    } else
      state.copy(
          groupedBlocks = filterUsedUniqueBlock(block, groupedBlocks),
          grid = grid + (nextPosition to block)
      )

    val nextBookmark = if (block == null)
      null // Consume bookmark
    else if (prioritySides.none()) {
      if (block.sides.any { (direction, side) ->
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
  assert(nextGrid.size > grid.size)
  nextGrid
}
