package scenery

import mythic.spatial.Matrix
import mythic.spatial.Vector3

interface Shape {
  val shapeHeight: Float
}

data class ShapeOffset(
    val transform: Matrix,
    val shape: Shape,
    override val shapeHeight: Float = shape.shapeHeight
) : Shape

data class Cylinder(
    val radius: Float,
    val height: Float,
    override val shapeHeight: Float = height
) : Shape

data class Capsule(
    val radius: Float,
    val height: Float,
    override val shapeHeight: Float = height
) : Shape

data class Sphere(
    val radius: Float,
    override val shapeHeight: Float = radius * 2f
) : Shape

data class Box(
    val halfExtents: Vector3,
    override val shapeHeight: Float = halfExtents.z * 2f
) : Shape
