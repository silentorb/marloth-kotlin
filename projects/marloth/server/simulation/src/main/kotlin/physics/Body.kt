package physics

import colliding.Cylinder
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import colliding.Shape
import colliding.Sphere
import mythic.spatial.isInsidePolygon
import simulation.*
import simulation.simulationDelta

data class BodyAttributes(
    val resistance: Float
)

private const val voidNodeHeight = 10000f

val voidNode: Node = Node(
    id = -1,
    position = Vector3(0f, 0f, -voidNodeHeight),
    height = voidNodeHeight,
    radius = 0f,
    isSolid = false,
    isWalkable = false,
    biome = Biome.void,
    floors = mutableListOf(),
    ceilings = mutableListOf(),
    walls = mutableListOf()
)

data class Body(
    override val id: Id,
    val shape: Shape?,
    val position: Vector3,
    val orientation: Quaternion,
    val velocity: Vector3 = Vector3(),
    val attributes: BodyAttributes,
    val gravity: Boolean,
//    val friction: Float,
    val node: Node,
    val perpetual: Boolean = false
) : Entity {

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

fun isInsideNode(node: Node, position: Vector3) =
    node.floors.any { isInsidePolygon(position, it.vertices) }
//        && position.z >= node.position.z
//        && position.z < node.position.z + node.height

fun updateBodyNode(realm: Realm, body: Body): Node {
  if (body.node == voidNode)
    return body.node

  if (body.velocity == Vector3.zero)
    return body.node

  val position = body.position
  val node = body.node

  val horizontalNode = if (!isInsideNodeHorizontally(node, position)) {
    val n = node.horizontalNeighbors.firstOrNull { isInsideNodeHorizontally(it, position) }
    if (n != null)
      n
    else {
      println("Warning: Had to rely on fallback method for determining which node a body is in.")
      val n3 = realm.nodes.filter { isInsideNode(it, position) }
      val n2 = realm.nodes.firstOrNull { isInsideNode(it, position) }
      if (n2 != null)
        n2
      else
        voidNode
    }
  } else
    node

  if (horizontalNode == voidNode)
    return voidNode

  val newNode = if (position.z < horizontalNode.position.z)
    if (horizontalNode.floors.any())
      getOtherNode(node, horizontalNode.floors.first())
    else
      voidNode
  else
    horizontalNode

  return if (newNode == null) {
//    isInsideNodeHorizontally(node, position)
//    throw Error("Not supported")
    assert(false)
    body.node
  } else {
    newNode
  }
}

fun updateBodies(world: World, commands: Commands, collisions: Collisions): List<Body> {
  val delta = simulationDelta
  val movementForces = allCharacterMovements(world, commands)
  val orientationForces = allCharacterOrientations(world)
  return updatePhysicsBodies(world, collisions, movementForces, orientationForces, delta)
}