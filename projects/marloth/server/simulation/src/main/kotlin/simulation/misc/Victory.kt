package simulation.misc

import mythic.ent.Id
import simulation.main.World
import simulation.main.defaultPlayer

data class GameOver(
    val winningFaction: Id
)

fun isVictory(world: World): Boolean {
  val body = world.deck.bodies[defaultPlayer(world.deck)]!!
  val node = world.realm.nodeTable[body.nearestNode]
  return node != null &&
      node.attributes.contains(NodeAttribute.exit) &&
      node.position.distance(body.position) < node.radius
}
