package mythic.spatial

private val _zero = Vector3i()
private val _unit = Vector3i(1, 1, 1)

data class Vector3i(
    val x: Int = 0,
    val y: Int = 0,
    val z: Int = 0
) {
  companion object {
    val zero: Vector3i = _zero
    val unit: Vector3i = _unit
  }
  operator fun plus(value: Vector3i): Vector3i = Vector3i(x + value.x, y + value.y, z + value.z)
  operator fun times(value: Vector3i): Vector3i = Vector3i(x * value.x, y * value.y, z * value.z)
  operator fun times(value: Int): Vector3i = Vector3i(x * value, y * value, z * value)
}
