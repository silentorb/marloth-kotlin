package generation.abstracted

import generation.general.*
import silentorb.mythic.ent.firstNotNull
import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.randomly.Dice
import simulation.misc.Cell
import simulation.misc.MapGrid
import simulation.misc.CellAttribute

data class BlockEntry(
    val origin: Vector3i,
    val position: Vector3i,
    val block: Block
)

typealias BlockHistory = List<BlockEntry>

data class SolvingState(
    val grid: BlockGrid,
    val history: BlockHistory,
    val blacklist: Set<BlockEntry>
)

fun rollback(step: BlockEntry, state: SolvingState): SolvingState {
  val (grid, history, blacklist) = state
  assert(history.any())
  println("- ${grid.size} ${step.position} ${openingCount(step.block)}")
  return SolvingState(
      grid = grid - step.position,
      history = history - step,
      blacklist = blacklist + step
  )
}

fun addStep(entry: BlockEntry, state: SolvingState): SolvingState {
  val (grid, history, blacklist) = state
  println("+ ${grid.size} ${entry.position} ${openingCount(entry.block)}")
  if (entry.position == Vector3i(x = 2, y = -2, z = 0)) {
    val k = 0
  }
  return SolvingState(
      grid = grid + (entry.position to entry.block),
      history = history + entry,
      blacklist = blacklist
  )
}

data class BlockGroups(
    val all: Set<Block>,
    val phases: List<Set<Block>>
)

fun newBlockGroups(blocks: Set<Block>): BlockGroups {
  val phases = blocks
      .groupBy(::openingCount)
      .entries
      .sortedBy { it.key }
      .map { it.value.toSet() }

  return BlockGroups(
      all = blocks,
      phases = phases
  )
}

fun addPathStep(
    maxSteps: Int,
    dice: Dice,
    blocks: Set<Block>,
    grid: BlockGrid,
    position: Vector3i
): BlockGrid {
  val incompleteSides = getIncompleteBlockSides(grid)
  val beyondMinimum = grid.size >= maxSteps
  val nextState = if (incompleteSides.none() && !beyondMinimum) {
    rollback(state.history.last(), state)
  } else {
//    val incompleteSide = dice.takeOne(incompleteSides)
    val incompleteSide = incompleteSides
        .maxBy { side ->
          history.indexOfLast { it.position == side.position }
        }!!
    val offset = directionVectors[incompleteSide.direction]!!
    val position = incompleteSide.position
    val nextPosition = position + offset
    assert(!grid.containsKey(nextPosition))
    val blacklisted = state.blacklist
        .filter { it.origin == position && it.position == nextPosition }
        .map { it.block }

    val block = if (!beyondMinimum)
      matchConnectingBlock(dice, blockGroups.all - blacklisted, grid, nextPosition)
    else
      blockGroups.phases
          .firstNotNull { blocks ->
            matchConnectingBlock(dice, blocks - blacklisted, grid, nextPosition)
          }

    if (block == null) {
      val step = state.history.lastOrNull { it.position == position }
      assert(step != null)
      rollback(step!!, state)
    } else {
      val entry = BlockEntry(
          origin = position,
          position = nextPosition,
          block = block
      )

      addStep(entry, state)
    }
  }
  return addPathStep(maxSteps, dice, blockGroups, nextState)
}

fun newWindingWorkbench(firstBlock: Block): Workbench {
  val startPosition = Vector3i.zero
  return Workbench(
      blockGrid = mapOf(
          startPosition to firstBlock
      ),
      mapGrid = MapGrid(
          cells = mapOf(
              startPosition to Cell(
                  attributes = setOf(CellAttribute.home),
                  slots = listOf()
              )
          )
      )
  )
}

fun windingPath(dice: Dice, config: BlockConfig, length: Int): (BlockGrid) -> BlockGrid = { grid ->
  val state = SolvingState(
      grid = grid,
      history = listOf(),
      blacklist = setOf()
  )
  val nextGrid = addPathStep(length - 1, dice, newBlockGroups(config.blocks), state)
  assert(nextGrid.size > grid.size)
  nextGrid
}
