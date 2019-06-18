package scenery

import mythic.spatial.Matrix
import mythic.spatial.Vector3

interface Shape {
  val shapeHeight: Float
}

class ShapeOffset(
    val transform: Matrix,
    val shape: Shape,
    override val shapeHeight: Float = shape.shapeHeight
) : Shape

class Cylinder(
    val radius: Float,
    val height: Float,
    override val shapeHeight: Float = height
) : Shape

class Capsule(
    val radius: Float,
    val height: Float,
    override val shapeHeight: Float = height
) : Shape

class Sphere(
    val radius: Float,
    override val shapeHeight: Float = radius * 2f
) : Shape

class Box(
    val halfExtents: Vector3,
    override val shapeHeight: Float = halfExtents.z * 2f
) : Shape
