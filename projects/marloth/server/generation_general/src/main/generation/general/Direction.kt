package generation.general

import silentorb.mythic.spatial.Vector3i

enum class Direction {
  up,
  down,
  east,
  north,
  west,
  south
}

val verticalDirectionVectors: Map<Direction, Vector3i> = mapOf(
    Direction.up to Vector3i(0, 0, 1),
    Direction.down to Vector3i(0, 0, -1)
)

val horizontalDirectionVectors: Map<Direction, Vector3i> = mapOf(
    Direction.east to Vector3i(1, 0, 0),
    Direction.north to Vector3i(0, 1, 0),
    Direction.west to Vector3i(-1, 0, 0),
    Direction.south to Vector3i(0, -1, 0)
)

val allDirectionVectors: Map<Direction, Vector3i> = verticalDirectionVectors.plus(horizontalDirectionVectors)

val horizontalDirections = horizontalDirectionVectors.keys
val verticalDirections = verticalDirectionVectors.keys
val allDirections = allDirectionVectors.keys

val horizontalDirectionList = listOf(Direction.east, Direction.north, Direction.west, Direction.south)

fun rotateDirection(turns: Int): (Direction) -> Direction = { direction ->
  val index = horizontalDirectionList.indexOf(direction)
  val newIndex = (index + turns) % 4
  horizontalDirectionList[newIndex]
}