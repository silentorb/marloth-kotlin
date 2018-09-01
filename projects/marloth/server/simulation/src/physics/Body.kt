package physics

import colliding.Cylinder
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import colliding.Shape
import colliding.Sphere
import mythic.spatial.isInsidePolygon
import scenery.Textures
import simulation.*
import simulation.changing.simulationDelta
import simulation.input.allPlayerMovements

data class BodyAttributes(
    val resistance: Float
)

private const val voidNodeHeight = 10000f

val voidNode: Node = Node(
    index = -1,
    position = Vector3(0f, 0f, -voidNodeHeight),
    height = voidNodeHeight,
    radius = 0f,
    isSolid = false,
    isWalkable = false,
    biome = Biome("void", floorTexture = Textures.none)
)

class Body(
    override val id: Id,
    val shape: Shape?,
    var position: Vector3,
    var orientation: Quaternion,
    var velocity: Vector3,
    val attributes: BodyAttributes,
    val gravity: Boolean,
//    val friction: Float,
    var node: Node
) : EntityLike {

  val radius: Float?
    get() = shape?.getRadiusOrNull()
}

val commonShapes = mapOf(
    EntityType.character to Cylinder(0.5f, 1f),
    EntityType.missile to Sphere(0.2f)
)

fun overlaps(first: Body, second: Body) =
    colliding.overlaps(first.shape, first.position, second.shape, second.position)

fun isInsideNodeHorizontally(node: Node, position: Vector3) =
    node.floors.any { isInsidePolygon(position, it.vertices) }

fun updateBodyNode(body: Body) {
  if (body.node == voidNode)
    return

  val position = body.position
  val node = body.node

  val horizontalNode = if (!isInsideNodeHorizontally(node, position)) {
    val n = node.horizontalNeighbors.firstOrNull { isInsideNodeHorizontally(it, position) }
    if (n != null)
      n
    else
      node
  } else
    node

  val newNode = if (position.z < horizontalNode.position.z)
    if (horizontalNode.floors.any())
      getOtherNode(node, horizontalNode.floors.first())
    else
      voidNode
  else
    horizontalNode

  if (newNode == null) {
//    isInsideNodeHorizontally(node, position)
//    throw Error("Not supported")
    assert(false)
  } else {
    body.node = newNode
  }
}

fun updateBodies(world: World, commands: Commands, collisions: Collisions): List<Body> {
  val delta = simulationDelta
  val forces = allPlayerMovements(world, commands)
  applyForces(forces, delta)
  updateBodies(world.bodies, collisions, delta)
  return world.bodies.map {
    updateBodyNode(it)
    it
  }
}
/*
fun getNewBodies(newEntities: NewEntities): List<Body> {
  return newEntities.newMissiles.map { newMissile ->
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
      .plus(newEntities.newCharacters.map { character ->
        Body(
            id = character.id,
            shape = commonShapes[EntityType.character]!!,
            position = character.position,
            orientation = Quaternion(),
            velocity = Vector3(),
            node = character.node,
            attributes = characterBodyAttributes,
            gravity = true
        )
      })
      .plus(newEntities.newFurnishings.map { character ->
        Body(
            id = character.id,
            shape = commonShapes[EntityType.character]!!,
            position = character.position,
            orientation = Quaternion(),
            velocity = Vector3(),
            node = character.node,
            attributes = characterBodyAttributes,
            gravity = true
        )
      })
}
*/