package generation.architecture.definition

import generation.elements.Side
import generation.elements.newBlock

val doorway: Side = setOf(ConnectionType.doorway)
val requiredOpen: Side = setOf(ConnectionType.doorway, ConnectionType.open)
val optionalOpen: Side = requiredOpen.plus(ConnectionType.impassable)
val impassable: Side = setOf(ConnectionType.impassable)
val any: Side = setOf()
val spiralStaircase: Side = setOf(ConnectionType.spiralStaircase)

class BlockDefinitions {
  companion object {

    val singleCellRoom = newBlock(
        top = impassable,
        bottom = impassable,
        east = optionalOpen,
        north = optionalOpen,
        west = optionalOpen,
        south = optionalOpen
    )

    val stairBottom = newBlock(
        top = spiralStaircase,
        bottom = impassable,
        east = optionalOpen,
        north = optionalOpen,
        south = optionalOpen,
        west = optionalOpen
    )

    val stairTop = newBlock(
        top = impassable,
        bottom = spiralStaircase,
        east = optionalOpen,
        north = optionalOpen,
        south = optionalOpen,
        west = optionalOpen
    )

    val stairMiddle = newBlock(
        top = spiralStaircase,
        bottom = spiralStaircase,
        east = impassable,
        north = impassable,
        south = impassable,
        west = impassable
    )
  }
}
