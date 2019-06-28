package physics

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
    radius = 0f,
    isSolid = false,
    isWalkable = false,
    biome = BiomeId.void
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
    override val position: Vector3,
    val velocity: Vector3 = Vector3.zero,
    val orientation: Quaternion,
    val scale: Vector3 = Vector3.unit,
    override val node: Id
) : SimpleBody

data class DynamicBody(
    val gravity: Boolean,
    val mass: Float,
    val resistance: Float,
    val friction: Float = 0.5f,
    val hinge: HingeConstraint? = null
)

fun updateBodies(world: World, commands: Commands, collisions: Collisions): Table<Body> {
  val delta = simulationDelta
  val movementForces = allCharacterMovements(world, commands)
  val orientationForces = allCharacterOrientations(world)
  return updatePhysicsBodies(world, collisions, movementForces, orientationForces, delta)
}

fun isInVoid(world: World, id: Id): Boolean =
    world.bodyTable[id]!!.node == voidNodeId
