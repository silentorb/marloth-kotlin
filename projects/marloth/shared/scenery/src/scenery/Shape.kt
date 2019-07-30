package scenery

import mythic.spatial.Matrix
import mythic.spatial.Vector3
import kotlin.math.max

interface Shape {
  val shapeHeight: Float
  val radius: Float
}

data class ShapeOffset(
    val transform: Matrix,
    val shape: Shape,
    override val shapeHeight: Float = shape.shapeHeight,
    override val radius: Float = shape.radius

) : Shape

data class Cylinder(
    override val radius: Float,
    val height: Float,
    override val shapeHeight: Float = height
) : Shape

data class Capsule(
    override val radius: Float,
    val height: Float,
    override val shapeHeight: Float = height
) : Shape

data class Sphere(
    override val radius: Float,
    override val shapeHeight: Float = radius * 2f
) : Shape

data class Box(
    val halfExtents: Vector3,
    override val shapeHeight: Float = halfExtents.z * 2f,
    override val radius: Float = max(max(halfExtents.x, halfExtents.y), halfExtents.z)
) : Shape
