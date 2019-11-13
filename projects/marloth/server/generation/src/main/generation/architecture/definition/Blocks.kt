package generation.architecture.definition

import generation.elements.Block
import generation.elements.Side
import generation.elements.enumerateMembers
import generation.elements.newBlock
import generation.next.Builder
import simulation.misc.NodeAttribute

val doorway: Side = setOf(ConnectionType.doorway)
val requiredOpen: Side = setOf(ConnectionType.doorway, ConnectionType.open)
val optionalOpen: Side = requiredOpen.plus(ConnectionType.wall)
val halfStepRequiredOpen: Side = setOf(ConnectionType.halfStepOpen)
val halfStepOptionalOpen: Side = halfStepRequiredOpen.plus(ConnectionType.wall)
val impassable: Side = setOf(ConnectionType.wall)
val any: Side = setOf()
val spiralStaircaseBottom: Side = setOf(ConnectionType.spiralStaircaseBottom)
val spiralStaircaseTop: Side = setOf(ConnectionType.spiralStaircaseTop)
val spiralStaircaseTopOrBottom: Side = setOf(ConnectionType.spiralStaircaseBottom, ConnectionType.spiralStaircaseTop)

class BlockDefinitions {
  companion object {

    val singleCellRoom = newBlock(
        up = impassable,
        down = impassable,
        east = optionalOpen,
        north = optionalOpen,
        west = optionalOpen,
        south = optionalOpen,
        attributes = setOf(NodeAttribute.fullFloor)
    )

    val stairBottom = newBlock(
        up = spiralStaircaseTopOrBottom,
        down = impassable,
        east = optionalOpen,
        north = optionalOpen,
        south = optionalOpen,
        west = impassable,
        attributes = setOf(NodeAttribute.lockedRotation)
    )

    val stairTop = newBlock(
        up = impassable,
        down = spiralStaircaseTopOrBottom,
        east = optionalOpen,
        north = optionalOpen,
        south = optionalOpen,
        west = impassable,
        attributes = setOf(NodeAttribute.lockedRotation)
    )

    val stairMiddle = newBlock(
        up = spiralStaircaseTop,
        down = spiralStaircaseBottom,
        east = impassable,
        north = impassable,
        south = impassable,
        west = impassable,
        attributes = setOf(NodeAttribute.lockedRotation)
    )

    val halfStepRoom = newBlock(
        up = impassable,
        down = impassable,
        east = halfStepOptionalOpen,
        north = halfStepOptionalOpen,
        west = halfStepOptionalOpen,
        south = halfStepOptionalOpen,
        attributes = setOf(NodeAttribute.fullFloor)
    )

    val lowerHalfStepSlope = newBlock(
        up = impassable,
        down = impassable,
        east = halfStepRequiredOpen,
        north = impassable,
        west = requiredOpen,
        south = impassable
    )
  }
}

fun allBlocks() = enumerateMembers<Block>(BlockDefinitions).toSet()

fun allBlocks(builders: Map<Block, Builder>) =
    allBlocks()
        .filter { builders.containsKey(it) }
        .toSet()
