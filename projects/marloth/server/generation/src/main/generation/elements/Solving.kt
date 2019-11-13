package generation.elements

import mythic.spatial.Vector3i
import randomly.Dice

typealias GetBlock = (Vector3i) -> Block?
typealias CheckBlockSide = (Map.Entry<Direction, Side>) -> Boolean

fun getOtherSide(getBlock: GetBlock, origin: Vector3i): (Direction) -> Side = { direction ->
  val oppositeSide = oppositeSides[direction]!!
  val offset = allDirections[direction]!!
  val position = origin + offset
  val block = getBlock(position)
  val sides = block?.sides
  sides?.getValue(oppositeSide) ?: setOf()
}

fun sidesMatch(surroundingSides: Map<Direction, Side>): CheckBlockSide = { (direction, blockSide) ->
  val otherSide = surroundingSides[direction]!!
  val result = otherSide.none() || otherSide.any { blockSide.contains(it) }
  if (!result) {
    val k = 0
  }
  result
}

fun checkBlockMatch(surroundingSides: Map<Direction, Side>): (Block) -> Boolean = { block ->
  block.sides.all(sidesMatch(surroundingSides))
}

fun getSurroundingSides(blocks: Set<Block>, workbench: Workbench, position: Vector3i): Sides {
  val getBlock: GetBlock = { workbench.blockGrid[it] }
  return allDirections.keys.associateWith(getOtherSide(getBlock, position))
}

fun filterOpenDirection(openConnections: Set<Any>, sides: Sides, direction: Direction): Sides {
  return sides.plus(Pair(direction, sides[direction]!!.intersect(openConnections)))
}

fun matchBlock(dice: Dice, blocks: Set<Block>, surroundingSides: Sides): Block? {
  val shuffledBlocks = dice.shuffle(blocks.toList())
  return shuffledBlocks.firstOrNull(checkBlockMatch(surroundingSides))
}

fun matchConnectingBlock(dice: Dice, blocks: Set<Block>, openConnections: Set<Any>, workbench: Workbench,
                         direction: Direction, position: Vector3i): Block? {
  val surroundingSides = getSurroundingSides(blocks, workbench, position)
  val modifiedSides = filterOpenDirection(openConnections, surroundingSides, oppositeSides[direction]!!)
  return matchBlock(dice, blocks, modifiedSides)
}
