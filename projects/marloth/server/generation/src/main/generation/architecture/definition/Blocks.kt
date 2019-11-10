package generation.architecture.definition

import generation.elements.Direction
import generation.elements.Side
import generation.elements.newBlock

val doorway: Side = setOf(ConnectionType.doorway)
val requiredOpen: Side = setOf(ConnectionType.doorway, ConnectionType.open)
val optionalOpen: Side = requiredOpen.plus(ConnectionType.impassable)
val impassable: Side = setOf(ConnectionType.impassable)
val greedySelf: Side = setOf()

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
        top = requiredOpen,
        bottom = impassable,
        east = optionalOpen,
        north = optionalOpen,
        south = optionalOpen,
        west = impassable
    )

    val stairTop = newBlock(
        top = impassable,
        bottom = requiredOpen,
        east = optionalOpen,
        north = optionalOpen,
        south = optionalOpen,
        west = impassable
    )
  }
}
