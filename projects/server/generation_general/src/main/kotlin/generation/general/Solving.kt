package generation.general

import generation.abstracted.GroupedBlocks
import silentorb.mythic.randomly.Dice
import silentorb.mythic.spatial.Vector3i
import simulation.misc.CellAttribute

typealias GetBlock = (Vector3i) -> BlockCell?

fun getOtherSide(getBlock: GetBlock, origin: Vector3i): (Direction) -> Side? = { direction ->
  val oppositeSide = oppositeDirections[direction]!!
  val offset = directionVectors[direction]!!
  val position = origin + offset
  val block = getBlock(position)
  val sides = block?.sides
  sides?.getOrDefault(oppositeSide, null)
}

fun sidesMatch(first: Side, second: Side): Boolean =
    first.other == second.mine &&
        first.mine == second.other &&
        first.height == second.height

fun sidesMatch(surroundingSides: SideMap, direction: Direction, blockSide: Side): Boolean {
  val otherSide = surroundingSides[direction]
  return otherSide != null && sidesMatch(blockSide, otherSide)
}

fun verticalTurnsAlign(otherTurns: Int?, turns: Int?): Boolean =
    otherTurns == null || turns == null || otherTurns == turns

fun getSurroundingSides(getBlock: GetBlock, location: Vector3i): SideMap {
  val getOther = getOtherSide(getBlock, location)
  return allDirections
      .mapNotNull {
        val side = getOther(it)
        if (side == null)
          null
        else
          it to side
      }
      .associate { it }
}

fun getSurroundingSides(blockGrid: BlockGrid, location: Vector3i): SideMap {
  val getBlock: GetBlock = { blockGrid[it]?.cell }
  return getSurroundingSides(getBlock, location)
}

fun checkBlockMatch(surroundingSides: SideMap, getBlock: GetBlock): (Block) -> Vector3i? = { block ->
  val match = block.traversable
      .firstOrNull { baseOffset ->
        block.cells
            .all { (cellOffset, cell) ->
              val appliedOffset = cellOffset - baseOffset
              val surroundingSides2 = if (appliedOffset == Vector3i.zero)
                surroundingSides
              else
                getSurroundingSides(getBlock, appliedOffset)

              surroundingSides2.all { side -> sidesMatch(cell.sides, side.key, side.value) }
            }
      }

  match
}

fun matchBlock(dice: Dice, blocks: Set<Block>, getBlock: GetBlock, surroundingSides: SideMap): Pair<Vector3i, Block>? {
  val shuffledBlocks = dice.shuffle(blocks)
  for (block in shuffledBlocks) {
    val offset = checkBlockMatch(surroundingSides, getBlock)(block)
    if (offset != null)
      return offset to block
  }
  return null
}

fun matchConnectingBlock(dice: Dice, blocks: Set<Block>, grid: BlockGrid, location: Vector3i): Pair<Vector3i, Block>? {
  val surroundingSides = getSurroundingSides(grid, location)
  val getBlock: GetBlock = { grid[it + location]?.cell }
  return matchBlock(dice, blocks, getBlock, surroundingSides)
}

fun fallbackBiomeMatchConnectingBlock(dice: Dice, biomeBlocks: Map<String, GroupedBlocks>, grid: BlockGrid,
                                      location: Vector3i, biome: String): Pair<Vector3i, Block>? {
  val options = directionVectors.values
      .mapNotNull { offset -> grid[location + offset]?.source?.biome }
      .distinct()
      .minus(biome)

  for (option in options) {
    val blocks = biomeBlocks[option]
    if (blocks != null) {
     val result = matchConnectingBlock(dice, blocks.all, grid, location)
      if (result != null) {
        return result
      }
    }
  }

  return null
}

fun openSides(blockGrid: BlockGrid, position: Vector3i): Map<Direction, Vector3i> {
  val block = blockGrid[position]!!
  return directionVectors
      .filter { direction ->
        val side = block.cell.sides[direction.key]
        side != null && !blockGrid.containsKey(position + direction.value)
      }
}

typealias UsableConnectionTypes = (Vector3i) -> (Direction) -> Side

fun getIncompleteBlockSides(blockGrid: BlockGrid, position: Vector3i): List<CellDirection> {
  val options = openSides(blockGrid, position)
  return options.map {
    CellDirection(
        cell = position,
        direction = it.key,
    )
  }
}

fun getIncompleteBlockSides(blockGrid: BlockGrid): List<CellDirection> =
    blockGrid.keys.flatMap { getIncompleteBlockSides(blockGrid, it) }

fun filterUsedUniqueBlocks(grid: BlockGrid, blocks: Set<Block>): Set<Block> {
  val uniqueNames = grid
      .filter { it.value.source.attributes.contains(CellAttribute.unique) }
      .values
      .map { it.source.name }
      .toSet()

  return blocks
      .filter { !uniqueNames.contains(it.name) }
      .toSet()
}
