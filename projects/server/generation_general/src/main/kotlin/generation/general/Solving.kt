package generation.general

import silentorb.mythic.spatial.Vector3i
import silentorb.mythic.randomly.Dice

typealias GetBlock = (Vector3i) -> Block?
typealias CheckBlockSide = (Map.Entry<Direction, Side>) -> Boolean

typealias SideCheck = (Side) -> Boolean

//fun isSideIndependent(independentConnections: Set<Any>): SideCheck = { side ->
//  side.any { independentConnections.contains(it) }
//}

//fun isBlockIndependent(isSideIndependent: SideCheck, directions: Set<Direction>): (Block) -> Boolean = { block ->
//  directions.all { isSideIndependent(block.sides.getValue(it)) }
//}

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
    if (blockSide.other == CoreSide.void)
      otherSide == null || (otherSide.mine == CoreSide.void && otherSide.other == blockSide.mine)
    else
      otherSide != null && otherSide.mine == blockSide.other && otherSide.other == blockSide.mine

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

//fun filterOpenDirection(openConnections: Set<Any>, sides: Sides, direction: Direction): Sides {
//  return sides.plus(Pair(direction, sides[direction]!!.intersect(openConnections)))
//}

fun matchBlock(dice: Dice, blocks: Set<Block>, surroundingSides: OptionalSides): Block? {
  val shuffledBlocks = dice.shuffle(blocks)
  return shuffledBlocks.firstOrNull(checkBlockMatch(surroundingSides))
}

fun matchConnectingBlock(dice: Dice, blocks: Set<Block>, workbench: Workbench, position: Vector3i): Block? {
  val surroundingSides = getSurroundingSides(workbench.blockGrid, position)
  return matchBlock(dice, blocks, surroundingSides)
}

fun openSides(blockGrid: BlockGrid, position: Vector3i): Map<Direction, Vector3i> {
  val block = blockGrid[position]!!
  return directionVectors
      .filter { direction ->
        block.sides[direction.key]!!.other != CoreSide.void &&
            !blockGrid.containsKey(position + direction.value)
      }
}

fun possibleNextDirections(blockGrid: BlockGrid, position: Vector3i): Map<Direction, Vector3i> {
  val block = blockGrid[position]!!
  val options = openSides(blockGrid, position)

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
  possibleNextDirections(blockGrid, position).any()
}

typealias UsableConnectionTypes = (Vector3i) -> (Direction) -> Side

//// Returns the intersection of af a block's sides with its neighbors' sides
//fun getUsableCellSide(independentConnectionTypes: Set<Any>, openConnectionTypes: Set<Any>,
//                      connections: ConnectionSet,
//                      blockGrid: BlockGrid): UsableConnectionTypes = { position ->
//  val surroundingSides = getSurroundingSides(blockGrid, position)
//  val block = blockGrid.getValue(position);
//  { direction ->
//    val side = block.sides.getValue(direction)
//    val otherSide = surroundingSides.getValue(direction)
//    if (otherSide != null) {
//      val intersection = side.intersect(otherSide)
//      val isConnected = containsConnection(connections, position, position + directionVectors[direction]!!)
//      if (isConnected)
//        intersection.intersect(openConnectionTypes)
//      else
//        intersection.minus(openConnectionTypes)
//    } else
//      side.intersect(independentConnectionTypes)
//  }
//}
