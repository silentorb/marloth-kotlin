package generation.architecture.definition

import generation.elements.Direction
import generation.elements.Side
import generation.elements.newBlock

val doorway: Side = setOf(ConnectionType.doorway)
val open: Side = setOf(ConnectionType.doorway)
val impassable: Side = setOf(ConnectionType.impassable)
val self: Side = setOf()

class BlockDefinitions {
  companion object {

    val singleCellRoom = newBlock(
        top = impassable,
        bottom = impassable,
        east = open,
        north = open,
        west = open,
        south = open
    )

    val stairBottom = singleCellRoom
        .plus(Direction.up to self)

    val stairTop = singleCellRoom
        .plus(Direction.down to self)
  }
}
