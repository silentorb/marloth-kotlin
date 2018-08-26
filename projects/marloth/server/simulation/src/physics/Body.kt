package physics

import colliding.Cylinder
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import colliding.Shape
import colliding.Sphere
import mythic.spatial.isInsidePolygon
import simulation.*
import simulation.changing.simulationDelta
import simulation.combat.NewMissile
import simulation.input.allPlayerMovements

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
    val gravity: Boolean,
//    val friction: Float,
    var node: Node
) {

  val radius: Float?
    get() = shape?.getRadiusOrNull()
}

val commonShapes = mapOf(
    EntityType.character to Cylinder(0.5f, 1f),
    EntityType.missile to Sphere(0.2f)
)

fun overlaps(first: Body, second: Body) =
    colliding.overlaps(first.shape, first.position, second.shape, second.position)

fun isInsideNode(node: Node, position: Vector3) =
    node.position.z <= position.z && node.position.z + node.height > position.z
        && node.floors.any { isInsidePolygon(position, it.vertices) }

fun updateBodyNode(body: Body) {
  val position = body.position
  val node = body.node

  if (isInsideNode(node, position))
    return

  val newNode = node.neighbors.firstOrNull { isInsideNode(it, position) }
  if (newNode == null) {
    isInsideNode(node, position)
//    throw Error("Not supported")
//    assert(false)
  } else {
    body.node = newNode
  }
}

fun updateBodies(world: World, commands: Commands): List<Body> {
  val delta = simulationDelta
  val forces = allPlayerMovements(world.characterTable, commands)
  applyForces(forces, delta)
  updateBodies(world.meta, world.bodies, delta)
  return world.bodies.map {
    updateBodyNode(it)
    it
  }
}

fun getNewBodies(newMissiles: List<NewMissile>): List<Body> {
  return newMissiles.map { newMissile ->
    Body(
        id = newMissile.id,
        shape = commonShapes[EntityType.missile]!!,
        position = newMissile.position,
        orientation = Quaternion(),
        velocity = newMissile.velocity,
        node = newMissile.node,
        attributes = missileBodyAttributes,
        gravity = false
    )
  }
}
