package generation.general

import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.randomly.Dice
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
    first.other.type == second.mine.type &&
        first.mine.type == second.other.type &&
        first.height == second.height &&
        (first.mine.biome == null || first.mine.biome == second.other.biome) &&
        (second.mine.biome == null || second.mine.biome == first.other.biome)

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
  val matches = block.cells.all { (offset, cell) ->
    val surroundingSides2 = if (offset == Vector3i.zero)
      surroundingSides
    else
      getSurroundingSides(getBlock, offset)

    surroundingSides2.all { side -> sidesMatch(cell.sides, side.key, side.value) }
  }
  if (matches)
    Vector3i.zero
  else
    null
}

fun matchBlock(dice: Dice, blocks: Set<Block>, getBlock: GetBlock, surroundingSides: SideMap): Pair<Vector3i, Block?> {
  val shuffledBlocks = dice.shuffle(blocks)
  for (block in shuffledBlocks) {
    val offset = checkBlockMatch(surroundingSides, getBlock)(block)
    if (offset != null)
      return offset to block
  }
  return Vector3i.zero to null
}

fun matchConnectingBlock(dice: Dice, blocks: Set<Block>, blockGrid: BlockGrid, location: Vector3i): Pair<Vector3i, Block?> {
  val surroundingSides = getSurroundingSides(blockGrid, location)
  val getBlock: GetBlock = { blockGrid[it + location]?.cell }
  return matchBlock(dice, blocks, getBlock, surroundingSides)
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

data class AbsoluteSide(
    val position: Vector3i,
    val direction: Direction
)

fun getIncompleteBlockSides(blockGrid: BlockGrid, position: Vector3i): List<AbsoluteSide> {
  val options = openSides(blockGrid, position)
  return options.map {
    AbsoluteSide(
        position = position,
        direction = it.key
    )
  }
}

fun getIncompleteBlockSides(blockGrid: BlockGrid): List<AbsoluteSide> =
    blockGrid.keys.flatMap { getIncompleteBlockSides(blockGrid, it) }

fun filterUsedUniqueBlocks(grid: BlockGrid, blocks: Set<Block>): Set<Block> {
  val uniqueNames = grid
      .filter { it.value.cell.attributes.contains(CellAttribute.unique) }
      .values
      .map { it.source.name }
      .toSet()

  return blocks
      .filter { !uniqueNames.contains(it.name) }
      .toSet()
}
