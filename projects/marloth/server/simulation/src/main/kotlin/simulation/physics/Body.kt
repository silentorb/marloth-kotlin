package simulation.physics

import mythic.ent.Id
import mythic.ent.Table
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import simulation.main.World
import simulation.main.simulationDelta
import simulation.input.Commands
import simulation.misc.Node
import simulation.entities.allCharacterMovements
import simulation.entities.allCharacterOrientations

private const val voidNodeHeight = 10000f

const val voidNodeId = -1L

//val voidNode: Node = Node(
//    id = voidNodeId,
//    position = Vector3(0f, 0f, -voidNodeHeight),
//    radius = 0f,
//    isSolid = false,
//    isWalkable = false,
//    isRoom = false
//)

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
    val orientation: Quaternion = Quaternion(),
    val scale: Vector3 = Vector3.unit,
    override val node: Id = voidNodeId
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
