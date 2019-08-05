package scenery

import mythic.spatial.Matrix
import mythic.spatial.Vector3
import kotlin.math.max

interface Shape {
  val radius: Float
  val x: Float
  val y: Float
  val height: Float
}

data class ShapeOffset(
    val transform: Matrix,
    val shape: Shape,
    override val height: Float = shape.height,
    override val radius: Float = shape.radius,
    override val x: Float = shape.x,
    override val y: Float = shape.y
) : Shape

data class Cylinder(
    override val radius: Float,
    override val height: Float,
    override val x: Float = radius * 2f,
    override val y: Float = radius * 2f
) : Shape

data class Capsule(
    override val radius: Float,
    override val height: Float,
    override val x: Float = radius * 2f,
    override val y: Float = radius * 2f
) : Shape

data class Sphere(
    override val radius: Float,
    override val height: Float = radius * 2f,
    override val x: Float = radius * 2f,
    override val y: Float = radius * 2f
) : Shape

data class Box(
    val halfExtents: Vector3,
    override val x: Float = halfExtents.x * 2f,
    override val y: Float = halfExtents.y * 2f,
    override val height: Float = halfExtents.z * 2f,
    override val radius: Float = max(max(halfExtents.x, halfExtents.y), halfExtents.z)
) : Shape
