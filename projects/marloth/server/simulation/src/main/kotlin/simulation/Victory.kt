package simulation

fun isVictory(world: World): Boolean {
  val body = world.deck.bodies[world.players.first().id]!!
  val node = world.realm.nodeTable[body.node]!!
  return node.biome == Biome.exit
}
