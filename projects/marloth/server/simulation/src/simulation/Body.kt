package simulation

import collision.Cylinder
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import collision.Shape
import collision.Sphere

class Body(
    val id: Id,
    var shape: Shape?,
    var position: Vector3,
    var orientation: Quaternion,
    var velocity: Vector3,
    var node: Node
)

val commonShapes = mapOf(
    EntityType.character to Cylinder(0.5f, 1f),
    EntityType.missile to Sphere(0.2f)
)

fun overlaps(first: Body, second: Body) =
    collision.overlaps(first.shape, first.position, second.shape, second.position)