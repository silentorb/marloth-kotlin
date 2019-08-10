package simulation.misc

import mythic.ent.Id
import simulation.main.World

data class GameOver(
    val winningFaction: Id
)

fun isVictory(world: World): Boolean {
  val body = world.deck.bodies[world.deck.players.keys.first()]!!
  val node = world.realm.nodeTable[body.nearestNode]
  return node != null &&
      node.attributes.contains(NodeAttribute.exit) &&
      node.position.distance(body.position) < node.radius
}
