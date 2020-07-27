package generation.general

import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.randomly.Dice
import simulation.misc.CellAttribute

typealias GetBlock = (Vector3i) -> Block?
typealias CheckBlockSide = (Map.Entry<Direction, Side>) -> Boolean

fun getOtherSide(getBlock: GetBlock, origin: Vector3i): (Direction) -> Side? = { direction ->
  val oppositeSide = oppositeDirections[direction]!!
  val offset = directionVectors[direction]!!
  val position = origin + offset
  val block = getBlock(position)
  val sides = block?.sides
  sides?.getValue(oppositeSide)
}

fun getOtherSide(grid: BlockGrid, origin: Vector3i): (Direction) -> Side? = { direction ->
  val oppositeSide = oppositeDirections[direction]!!
  val offset = directionVectors[direction]!!
  val position = origin + offset
  val block = grid[position]
  val sides = block?.sides
  sides?.getValue(oppositeSide)
}

fun sidesMatch(blockSide: Side, otherSide: Side?): Boolean =
    if (blockSide.other.contains(CoreSide.void))
      otherSide == null || (otherSide.mine == CoreSide.void && otherSide.other.contains(blockSide.mine))
    else
      otherSide == null || (blockSide.other.contains(otherSide.mine) && otherSide.other.contains(blockSide.mine))

fun sidesMatch(surroundingSides: OptionalSides): CheckBlockSide = { (direction, blockSide) ->
  val otherSide = surroundingSides[direction]
  sidesMatch(blockSide, otherSide)
}

fun checkBlockMatch(surroundingSides: OptionalSides): (Block) -> Boolean = { block ->
  block.sides.all(sidesMatch(surroundingSides))
}

fun getSurroundingSides(blockGrid: BlockGrid, position: Vector3i): OptionalSides {
  val getBlock: GetBlock = { blockGrid[it] }
  return allDirections.associateWith(getOtherSide(getBlock, position))
}

fun matchBlock(dice: Dice, blocks: Set<Block>, surroundingSides: OptionalSides): Block? {
  val shuffledBlocks = dice.shuffle(blocks)
  val result = shuffledBlocks.firstOrNull(checkBlockMatch(surroundingSides))
  return result
}

fun matchConnectingBlock(dice: Dice, blocks: Set<Block>, blockGrid: BlockGrid, position: Vector3i): Block? {
  val surroundingSides = getSurroundingSides(blockGrid, position)
  return matchBlock(dice, blocks, surroundingSides)
}

fun openSides(blockGrid: BlockGrid, position: Vector3i): Map<Direction, Vector3i> {
  val block = blockGrid[position]!!
  return directionVectors
      .filter { direction ->
        !block.sides[direction.key]!!.other.contains(CoreSide.void) &&
            !blockGrid.containsKey(position + direction.value)
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
      .filter { it.value.attributes.contains(CellAttribute.unique) }
      .values
      .map { it.name }
      .toSet()

  return blocks
      .filter { !uniqueNames.contains(it.name) }
      .toSet()
}
