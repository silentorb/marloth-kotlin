package generation.elements

import mythic.spatial.Vector3i

enum class Direction {
  up,
  down,
  east,
  north,
  west,
  south
}

val verticalDirections: Map<Direction, Vector3i> = mapOf(
    Direction.up to Vector3i(0, 0, 1),
    Direction.down to Vector3i(0, 0, -1)
)

val horizontalDirections: Map<Direction, Vector3i> = mapOf(
    Direction.east to Vector3i(1, 0, 0),
    Direction.north to Vector3i(0, 1, 0),
    Direction.west to Vector3i(-1, 0, 0),
    Direction.south to Vector3i(0, -1, 0)
)

val sideDirections: Map<Direction, Vector3i> = verticalDirections.plus(horizontalDirections)
