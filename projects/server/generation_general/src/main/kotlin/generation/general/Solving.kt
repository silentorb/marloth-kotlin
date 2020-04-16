package generation.general

import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.randomly.Dice
import simulation.misc.ConnectionSet
import simulation.misc.containsConnection

typealias GetBlock = (Vector3i) -> Block?
typealias CheckBlockSide = (Map.Entry<Direction, Side>) -> Boolean

typealias SideCheck = (Side) -> Boolean

fun isSideIndependent(independentConnections: Set<Any>): SideCheck = { side ->
  side.any { independentConnections.contains(it) }
}

fun isBlockIndependent(isSideIndependent: SideCheck, directions: Set<Direction>): (Block) -> Boolean = { block ->
  directions.all { isSideIndependent(block.sides.getValue(it)) }
}

fun getOtherSide(getBlock: GetBlock, origin: Vector3i): (Direction) -> Side = { direction ->
  val oppositeSide = oppositeDirections[direction]!!
  val offset = directionVectors[direction]!!
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

fun getSurroundingSides(blockGrid: BlockGrid, position: Vector3i): Sides {
  val getBlock: GetBlock = { blockGrid[it] }
  return allDirections.associateWith(getOtherSide(getBlock, position))
}

fun filterOpenDirection(openConnections: Set<Any>, sides: Sides, direction: Direction): Sides {
  return sides.plus(Pair(direction, sides[direction]!!.intersect(openConnections)))
}

fun matchBlock(dice: Dice, blocks: Set<Block>, surroundingSides: Sides): Block? {
  val shuffledBlocks = dice.shuffle(blocks)
  return shuffledBlocks.firstOrNull(checkBlockMatch(surroundingSides))
}

fun matchConnectingBlock(dice: Dice, blocks: Set<Block>, openConnections: Set<Any>, workbench: Workbench,
                         direction: Direction, position: Vector3i): Block? {
  val surroundingSides = getSurroundingSides(workbench.blockGrid, position)
  val modifiedSides = filterOpenDirection(openConnections, surroundingSides, oppositeDirections[direction]!!)
  return matchBlock(dice, blocks, modifiedSides)
}

fun openSides(config: BlockConfig, blockGrid: BlockGrid, position: Vector3i): Map<Direction, Vector3i> {
  val block = blockGrid[position]!!
  return directionVectors
      .filter { direction -> !blockGrid.containsKey(position + direction.value) }
      .filter { direction -> isSideOpen(config.openConnections, block.sides.getValue(direction.key)) }
}

fun possibleNextDirections(config: BlockConfig, blockGrid: BlockGrid, position: Vector3i): Map<Direction, Vector3i> {
  val block = blockGrid[position]!!
  val options = openSides(config, blockGrid, position)

//  val essential = options.filter { direction ->
//    val side = block.sides[direction.key]!!
//    !config.isSideIndependent(side)
//  }

//  return if (essential.any())
//    essential
//  else
   return options
}

fun blockCanHaveMoreConnections(config: BlockConfig, blockGrid: BlockGrid): (Vector3i) -> Boolean = { position ->
  possibleNextDirections(config, blockGrid, position).any()
}

typealias UsableConnectionTypes = (Vector3i) -> (Direction) -> Side

// Returns the intersection of af a block's sides with its neighbors' sides
fun getUsableCellSide(independentConnectionTypes: Set<Any>, openConnectionTypes: Set<Any>,
                      connections: ConnectionSet,
                      blockGrid: BlockGrid): UsableConnectionTypes = { position ->
  val surroundingSides = getSurroundingSides(blockGrid, position)
  val block = blockGrid.getValue(position);
  { direction ->
    val side = block.sides.getValue(direction)
    val otherSide = surroundingSides.getValue(direction)
    if (otherSide.any()) {
      val intersection = side.intersect(otherSide)
      val isConnected = containsConnection(connections, position, position + directionVectors[direction]!!)
      if (isConnected)
        intersection.intersect(openConnectionTypes)
      else
        intersection.minus(openConnectionTypes)
    } else
      side.intersect(independentConnectionTypes)
  }
}
