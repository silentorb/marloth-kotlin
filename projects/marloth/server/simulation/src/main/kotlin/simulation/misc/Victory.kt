package simulation.misc

import mythic.ent.Id
import simulation.main.World

data class GameOver(
    val winningFaction: Id
)

fun isVictory(world: World): Boolean {
//  val body = world.deck.bodies[world.players.first().id]!!
//  val node = world.realm.nodeTable[body.node]!!
//  return node.biome == BiomeId.exit
  return false
}
