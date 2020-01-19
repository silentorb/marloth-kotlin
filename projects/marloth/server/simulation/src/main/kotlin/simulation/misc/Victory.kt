package simulation.misc

import silentorb.mythic.debugging.getDebugString
import silentorb.mythic.ent.Id
import simulation.main.World

data class GameOver(
    val winningFaction: Id
)

fun isVictory(world: World): Boolean {
  if (getDebugString("DISABLE_VICTORY") != null) return false
  return false
//  val body = world.deck.bodies[defaultPlayer(world.deck)]!!
//  val node = world.realm.nodeTable[body.nearestNode]
//  return node != null &&
//      node.attributes.contains(NodeAttribute.exit) &&
//      node.position.distance(body.position) < node.radius
}
