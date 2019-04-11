package colliding

import mythic.spatial.Vector2
import mythic.spatial.Vector3

interface Shape {
}

class Cylinder(
    val radius: Float,
    val height: Float
) : Shape {
}

class Sphere(
    val radius: Float
) : Shape {
}
