package generation.architecture.definition

import generation.elements.Block
import generation.elements.Side
import generation.elements.enumerateMembers
import generation.elements.newBlock

val doorway: Side = setOf(ConnectionType.doorway)
val requiredOpen: Side = setOf(ConnectionType.doorway, ConnectionType.open)
val optionalOpen: Side = requiredOpen.plus(ConnectionType.impassable)
val halfStepRequiredOpen: Side = setOf(ConnectionType.halfStepOpen)
val halfStepOptionalOpen: Side = halfStepRequiredOpen.plus(ConnectionType.impassable)
val impassable: Side = setOf(ConnectionType.impassable)
val any: Side = setOf()
val spiralStaircase: Side = setOf(ConnectionType.spiralStaircase)

class BlockDefinitions {
  companion object {

    val singleCellRoom = newBlock(
        up = impassable,
        down = impassable,
        east = optionalOpen,
        north = optionalOpen,
        west = optionalOpen,
        south = optionalOpen
    )

    val stairBottom = newBlock(
        up = spiralStaircase,
        down = impassable,
        east = optionalOpen,
        north = optionalOpen,
        south = optionalOpen,
        west = optionalOpen
    )

    val stairTop = newBlock(
        up = impassable,
        down = spiralStaircase,
        east = optionalOpen,
        north = optionalOpen,
        south = optionalOpen,
        west = optionalOpen
    )

    val stairMiddle = newBlock(
        up = spiralStaircase,
        down = spiralStaircase,
        east = impassable,
        north = impassable,
        south = impassable,
        west = impassable
    )

    val halfStepRoom = newBlock(
        up = impassable,
        down = impassable,
        east = halfStepOptionalOpen,
        north = halfStepOptionalOpen,
        west = halfStepOptionalOpen,
        south = halfStepOptionalOpen
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
