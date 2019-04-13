package physics

import mythic.ent.Entity
import mythic.ent.Id
import mythic.ent.Table
import mythic.sculpting.ImmutableFaceTable
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.isInsidePolygon
import simulation.*

private const val voidNodeHeight = 10000f

const val voidNodeId = -1L

val voidNode: Node = Node(
    id = voidNodeId,
    position = Vector3(0f, 0f, -voidNodeHeight),
    height = voidNodeHeight,
    radius = 0f,
    isSolid = false,
    isWalkable = false,
    biome = Biome.void
)

interface SimpleBody {
  val position: Vector3
  val node: Id
}

data class HingeConstraint(
    val pivot: Vector3,
    val axis: Vector3
)

data class Body(
    override val id: Id,
    override val position: Vector3,
    val velocity: Vector3 = Vector3.zero,
    val orientation: Quaternion,
    val scale: Vector3 = Vector3.unit,
    override val node: Id
) : Entity, SimpleBody

data class DynamicBody(
    val gravity: Boolean,
    val mass: Float,
    val resistance: Float,
    val friction: Float = 0.5f,
    val hinge: HingeConstraint? = null
)

fun isInsideNodeHorizontally(faces: ImmutableFaceTable, node: Node, position: Vector3) =
    node.floors.any { isInsidePolygon(position, faces[it]!!.vertices) }

fun isInsideNode(faces: ImmutableFaceTable, node: Node, position: Vector3) =
    node.floors.any { isInsidePolygon(position, faces[it]!!.vertices) }
//        && position.z >= node.position.z
//        && position.z < node.position.z + node.height

fun updateBodyNode(realm: Realm, body: Body): Id {
  if (body.node == voidNodeId)
    return body.node

  if (body.velocity == Vector3.zero)
    return body.node

  val position = body.position
  val node = realm.nodeTable[body.node]!!

  val horizontalNode = if (!isInsideNodeHorizontally(realm.mesh.faces, node, position)) {
    val n = horizontalNeighbors(realm.faces, node)
        .map { realm.nodeTable[it]!! }
        .firstOrNull { isInsideNodeHorizontally(realm.mesh.faces, it, position) }

    if (n != null)
      n
    else {
      println("Warning: Had to rely on fallback method for determining which node a body is in.")
      val n3 = realm.nodeList.filter { isInsideNode(realm.mesh.faces, it, position) }
      val n2 = realm.nodeList.firstOrNull { isInsideNode(realm.mesh.faces, it, position) }
      if (n2 != null)
        n2
      else
        voidNode
    }
  } else
    node

  if (horizontalNode.id == voidNodeId)
    return voidNodeId

  val newNode = if (position.z < horizontalNode.position.z) {
    val floors = getFloors(realm.faces.values, horizontalNode)
    if (floors.any())
      realm.nodeTable[getOtherNode(node.id, floors.first())]
    else
      voidNode
  } else
    horizontalNode

  return if (newNode == null) {
//    isInsideNodeHorizontally(node, position)
//    throw Error("Not supported")
    assert(false)
    body.node
  } else {
//    if (newNode.id != node.id && !newNode.isWalkable) {
//      assert(false)
//    }
//
//    if (newNode.id != node.id && newNode.biome == Biome.void) {
//      assert(false)
//    }
    newNode.id
  }
}

fun updateBodies(world: World, commands: Commands, collisions: Collisions): Table<Body> {
  val delta = simulationDelta
  val movementForces = allCharacterMovements(world, commands)
  val orientationForces = allCharacterOrientations(world)
  return updatePhysicsBodies(world, collisions, movementForces, orientationForces, delta)
}

fun isInVoid(world: World, id: Id): Boolean =
    world.bodyTable[id]!!.node == voidNodeId