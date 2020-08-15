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

fun verticalTurnsAlign(otherTurns: Int?, turns: Int?): Boolean =
    otherTurns == null || turns == null || otherTurns == turns

fun checkBlockMatch(surroundingSides: OptionalSides, upTurns: Int?, downTurns: Int?): (Block) -> Boolean = { block ->
  block.sides.all(sidesMatch(surroundingSides)) &&
      verticalTurnsAlign(upTurns, block.turns) &&
      verticalTurnsAlign(downTurns, block.turns)
}

fun getSurroundingSides(blockGrid: BlockGrid, location: Vector3i): OptionalSides {
  val getBlock: GetBlock = { blockGrid[it] }
  return allDirections.associateWith(getOtherSide(getBlock, location))
}

fun matchBlock(dice: Dice, blocks: Set<Block>, surroundingSides: OptionalSides, upTurns: Int?, downTurns: Int?): Block? {
  val shuffledBlocks = dice.shuffle(blocks)
  val result = shuffledBlocks.firstOrNull(checkBlockMatch(surroundingSides, upTurns, downTurns))
  return result
}

fun matchConnectingBlock(dice: Dice, blocks: Set<Block>, blockGrid: BlockGrid, location: Vector3i): Block? {
  val surroundingSides = getSurroundingSides(blockGrid, location)
  val upTurns = blockGrid[location + directionVectors[Direction.up]!!]?.turns
  val downTurns = blockGrid[location + directionVectors[Direction.down]!!]?.turns
  return matchBlock(dice, blocks, surroundingSides, upTurns, downTurns)
}

fun openSides(blockGrid: BlockGrid, position: Vector3i): Map<Direction, Vector3i> {
  val block = blockGrid[position]!!
  return directionVectors
      .filter { direction ->
        val side = block.sides[direction.key]!!
        side.connectionLogic != ConnectionLogic.minimal &&
            !side.other.contains(CoreSide.void) &&
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
