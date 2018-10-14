package colliding

import mythic.spatial.Vector2
import mythic.spatial.Vector3

interface Shape {
  fun getRadiusOrNull(): Float?
}

class Cylinder(
    val radius: Float,
    val height: Float
) : Shape {
  override fun getRadiusOrNull(): Float? = radius
}

class Sphere(
    val radius: Float
) : Shape {
  override fun getRadiusOrNull(): Float? = radius
}

fun overlaps(firstPosition: Vector2, firstRadius: Float, secondPosition: Vector2, secondRadius: Float): Boolean =
    firstPosition.distance(secondPosition) - firstRadius - secondRadius < 0f

fun overlaps(first: Shape?, firstPosition: Vector3, second: Shape?, secondPosition: Vector3): Boolean {
  if (first == null || second == null)
    return false

  val firstRadius = first.getRadiusOrNull()
  val secondRadius = second.getRadiusOrNull()
  return if (firstRadius != null && secondRadius != null) {
    overlaps(firstPosition.xy(), firstRadius, secondPosition.xy(), secondRadius)
  } else {
    throw Error("Not implemented")
  }
}