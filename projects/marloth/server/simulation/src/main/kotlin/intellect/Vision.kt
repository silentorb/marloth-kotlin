package intellect

import colliding.rayCanHitPoint
import physics.Body
import simulation.Character
import simulation.World

const val viewingRange = 30f

fun isInAngleOfView(viewer: Character, viewerBody: Body, targetBody: Body): Boolean =
    viewer.facingVector.dot((targetBody.position - viewerBody.position).normalize()) > 0.5f

fun isInVisibleRange(viewerBody: Body, targetBody: Body): Boolean =
    viewerBody.position.distance(targetBody.position) <= viewingRange

fun canSee(world: World, viewer: Character, target: Character): Boolean {
  val viewerBody = world.bodyTable[viewer.id]!!
  val targetBody = world.bodyTable[target.id]!!
  val nodes = world.realm.nodeTable
  return isInVisibleRange(viewerBody, targetBody)
      && isInAngleOfView(viewer, viewerBody, targetBody)
      && rayCanHitPoint(world.realm, nodes[viewerBody.node]!!, viewerBody.position, targetBody.position)
}
