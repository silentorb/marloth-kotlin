package generation.elements

import mythic.spatial.Vector3i
import randomly.Dice

typealias GetBlock = (Vector3i) -> Block?
typealias CheckBlockSide = (Map.Entry<Direction, Side>) -> Boolean

fun getOtherSide(getBlock: GetBlock, origin: Vector3i, direction: Direction): Side? {
  val oppositeSide = oppositeSides[direction]!!
  val offset = allDirections[direction]!!
  val position = origin + offset
  val block = getBlock(position)
  val sides = block?.sides
  return if (sides == null) null else sides[oppositeSide]
}

fun sidesMatch(getBlock: GetBlock, origin: Vector3i): CheckBlockSide = { (direction, side) ->
  val otherSide = getOtherSide(getBlock, origin, direction) ?: setOf()
  val result = side.none() || otherSide.none() || otherSide.any { side.contains(it) }
  result
}

fun checkBlockMatch(getBlock: GetBlock, position: Vector3i): (Block) -> Boolean = { block ->
  block.sides.all(sidesMatch(getBlock, position))
}

fun matchBlock(dice: Dice, blocks: Set<Block>, workbench: Workbench, position: Vector3i): Block? {
  val getBlock: GetBlock = { workbench.blockGrid[it] }
  val shuffledBlocks = dice.shuffle(blocks.toList())
  return shuffledBlocks.firstOrNull(checkBlockMatch(getBlock, position))
}
