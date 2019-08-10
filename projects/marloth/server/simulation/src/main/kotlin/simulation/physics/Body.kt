package simulation.physics

import mythic.ent.Id
import mythic.ent.firstSortedBy
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import simulation.main.World
import simulation.misc.Realm

const val voidNodeId = -1L

interface SimpleBody {
  val position: Vector3
  val nearestNode: Id
}

data class HingeConstraint(
    val pivot: Vector3,
    val axis: Vector3
)

data class Body(
    override val position: Vector3,
    val velocity: Vector3 = Vector3.zero,
    val orientation: Quaternion = Quaternion(),
    val scale: Vector3 = Vector3.unit,
    override val nearestNode: Id = voidNodeId
) : SimpleBody

data class DynamicBody(
    val gravity: Boolean,
    val mass: Float,
    val resistance: Float,
    val friction: Float = 0.5f,
    val hinge: HingeConstraint? = null
)

fun updateNearestBodyNode(realm: Realm, body: Body): Id {
  val currentNode = realm.nodeTable[body.nearestNode]
  return if (currentNode != null && body.position.distance(currentNode.position) <= currentNode.radius)
    currentNode.id
  else {
    val nearest = realm.nodeTable.values.firstSortedBy { it.position.distance(body.position) - it.radius }
    nearest.id
  }
}

fun updateBody(realm: Realm): (Body) -> Body = { body ->
  body.copy(
      nearestNode = updateNearestBodyNode(realm, body)
  )
}

fun isInVoid(world: World, id: Id): Boolean =
    world.deck.bodies[id]!!.nearestNode == voidNodeId
