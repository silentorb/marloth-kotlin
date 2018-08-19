package physics

import colliding.Cylinder
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import colliding.Shape
import colliding.Sphere
import simulation.EntityType
import simulation.Id
import simulation.Node

data class BodyAttributes(
    val resistance: Float
)

class Body(
    val id: Id,
    val shape: Shape?,
    var position: Vector3,
    var orientation: Quaternion,
    var velocity: Vector3,
    val attributes: BodyAttributes,
//    val friction: Float,
    var node: Node
)

val commonShapes = mapOf(
    EntityType.character to Cylinder(0.5f, 1f),
    EntityType.missile to Sphere(0.2f)
)

fun overlaps(first: Body, second: Body) =
    colliding.overlaps(first.shape, first.position, second.shape, second.position)