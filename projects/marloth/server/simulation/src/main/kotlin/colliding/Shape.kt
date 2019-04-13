package colliding

import mythic.spatial.Matrix
import mythic.spatial.Vector3

interface Shape

class ShapeOffset(
    val transform: Matrix,
    val shape: Shape
) : Shape

class Cylinder(
    val radius: Float,
    val height: Float
) : Shape

class Capsule(
    val radius: Float,
    val height: Float
) : Shape

class Sphere(
    val radius: Float
) : Shape

class Box(
    val halfExtents: Vector3
) : Shape