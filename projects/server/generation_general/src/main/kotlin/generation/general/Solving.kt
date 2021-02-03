package generation.general

import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.randomly.Dice
import simulation.misc.CellAttribute

typealias GetBlock = (Vector3i) -> BlockCell?
typealias CheckBlockSide = (Map.Entry<Direction, Side>) -> Boolean

fun getOtherSide(getBlock: GetBlock, origin: Vector3i): (Direction) -> Side? = { direction ->
  val oppositeSide = oppositeDirections[direction]!!
  val offset = directionVectors[direction]!!
  val position = origin + offset
  val block = getBlock(position)
  val sides = block?.sides
  sides?.getOrDefault(oppositeSide, null)
}

//fun getOtherSide(grid: BlockGrid, origin: Vector3i): (Direction) -> Side? = { direction ->
//  val oppositeSide = oppositeDirections[direction]!!
//  val offset = directionVectors[direction]!!
//  val position = origin + offset
//  val block = grid[position]
//  val sides = block?.sidesOld
//  sides?.getValue(oppositeSide)
//}

fun sidesMatch(first: Side, second: Side): Boolean =
        first.other.type == second.mine.type &&
        first.mine.type == second.other.type &&
        first.height == second.height &&
        (first.mine.biome == null || first.mine.biome == second.other.biome) &&
        (second.mine.biome == null || second.mine.biome == first.other.biome)

fun sidesMatch(surroundingSides: OptionalSides): CheckBlockSide = { (direction, blockSide) ->
  val otherSide = surroundingSides[direction]
  otherSide == null || sidesMatch(blockSide, otherSide)
}

fun verticalTurnsAlign(otherTurns: Int?, turns: Int?): Boolean =
    otherTurns == null || turns == null || otherTurns == turns

fun checkBlockMatch(surroundingSides: OptionalSides): (Block) -> Boolean = { block ->
  block.sidesOld.all(sidesMatch(surroundingSides))
}

fun getSurroundingSides(blockGrid: BlockGrid, location: Vector3i): OptionalSides {
  val getBlock: GetBlock = { blockGrid[it]?.cell }
  return allDirections.associateWith(getOtherSide(getBlock, location))
}

fun matchBlock(dice: Dice, blocks: Set<Block>, surroundingSides: OptionalSides): Block? {
  val shuffledBlocks = dice.shuffle(blocks)
  val result = shuffledBlocks.firstOrNull(checkBlockMatch(surroundingSides))
  return result
}

fun matchConnectingBlock(dice: Dice, blocks: Set<Block>, blockGrid: BlockGrid, location: Vector3i): Block? {
  val surroundingSides = getSurroundingSides(blockGrid, location)
  return matchBlock(dice, blocks, surroundingSides)
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
