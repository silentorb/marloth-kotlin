package generation.general

import silentorb.mythic.spatial.Vector3i

fun getTurnDirection(turns: Int): Direction? =
    when ((turns + 4) % 4) {
      0 -> Direction.east
      1 -> Direction.north
      2 -> Direction.west
      3 -> Direction.south
      else -> null
    }

fun rotateZ(turns: Int, value: Direction): Direction =
    getTurnDirection(horizontalDirectionList.indexOf(value) + turns) ?: value

fun rotateZ(turns: Int, value: Vector3i): Vector3i {
  val (x, y, z) = value
  return when ((turns + 4) % 4) {
    3 -> Vector3i(y, -x, z)
    2 -> Vector3i(-x, -y, z)
    1 -> Vector3i(-y, x, z)
    else -> value
  }
}

fun rotateZ(turns: Int, cellDirection: CellDirection): CellDirection =
    cellDirection.copy(
        direction = rotateZ(turns, cellDirection.direction)
    )
