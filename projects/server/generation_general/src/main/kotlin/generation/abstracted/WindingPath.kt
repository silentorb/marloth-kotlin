package generation.abstracted

import generation.general.*
import silentorb.mythic.randomly.Dice
import simulation.misc.CellAttribute

data class GroupedBlocks(
    val all: Set<Block>,
    val polyominoes: Set<Block>,
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
      polyominoes = polyominoes,
      expansive = expansive,
      flexible = expansive - polyominoes
  )
}

fun filterUsedUniqueBlock(block: Block?, groupedBlocks: GroupedBlocks): GroupedBlocks =
    if (block != null && block.attributes.contains(CellAttribute.unique))
      GroupedBlocks(
          all = groupedBlocks.all - block,
          polyominoes = groupedBlocks.polyominoes - block,
          expansive = groupedBlocks.expansive - block,
          flexible = groupedBlocks.flexible - block
      )
    else
      groupedBlocks

data class BlockState(
    val groupedBlocks: GroupedBlocks,
    val grid: BlockGrid,
    val blacklist: List<AbsoluteSide>
)

tailrec fun addPathStep(
    maxSteps: Int,
    dice: Dice,
    state: BlockState,
    bookMark: BlockState? = null,
    safe: Boolean = false
): BlockGrid {
  val (groupedBlocks, grid, blacklist) = state
  val incompleteSides = getIncompleteBlockSides(grid) - blacklist
  if (incompleteSides.none())
    return grid

  val sideGroups = incompleteSides.groupBy {
    grid[it.position]!!.sides[it.direction]!!.connectionLogic
  }
  val prioritySides = sideGroups[ConnectionLogic.required] ?: listOf()

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

    val blocks = if (safe || incompleteSides.size < 2)
      if (grid.size >= maxSteps)
        groupedBlocks.flexible
      else
        groupedBlocks.expansive
    else
      groupedBlocks.all

    val block = matchConnectingBlock(dice, blocks, grid, nextPosition)
        ?: matchConnectingBlock(dice, groupedBlocks.all, grid, nextPosition)

    val nextState = if (block == null) {
      bookMark ?: state.copy(blacklist = blacklist + incompleteSide)
    } else
      BlockState(
          groupedBlocks = filterUsedUniqueBlock(block, groupedBlocks),
          grid = grid + (nextPosition to block),
          blacklist = blacklist
      )

    val nextBookmark = if (block == null)
      null // Consume bookmark
    else if (prioritySides.none() && groupedBlocks.polyominoes.contains(block)) {
      assert(bookMark == null)
      state // Place bookmark
    } else
      bookMark // Persist bookmark

    addPathStep(maxSteps, dice, nextState, nextBookmark, block == null)
  }
}

fun windingPath(dice: Dice, config: BlockConfig, length: Int): (BlockGrid) -> BlockGrid = { grid ->
  val blocks = filterUsedUniqueBlocks(grid, config.blocks)
  val groupedBlocks = newGroupedBlocks(blocks)
  val nextGrid = addPathStep(length, dice, BlockState(groupedBlocks, grid, listOf()))
  assert(nextGrid.size > grid.size)
  nextGrid
}
