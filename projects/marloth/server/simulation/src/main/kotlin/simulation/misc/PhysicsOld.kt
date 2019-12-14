package simulation.misc

import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.*

data class Rotation(
    val pitch: Float = 0f,
    val yaw: Float = 0f,
    val roll: Float = 0f
)

//fun updateBody(body: Body, orientationForces: List<AbsoluteOrientationForce>): Body {
//  return body.copy(
//      orientation = orientationForces.firstOrNull()?.orientation ?: body.orientation
//  )
//}
//
//fun updatePhysicsBodies(world: World, orientationForces: List<AbsoluteOrientationForce>): Table<Body> {
//  val updated = world.deck.dynamicBodies.mapValues { (id, _) ->
//    val body = world.deck.bodies[id]!!
//    updateBody(
//        body = body,
//        orientationForces = orientationForces.filter { it.body == id }
//    )
//  }
//  return world.deck.bodies.plus(updated)
//}
