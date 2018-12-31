package marloth.clienting.audio

import scenery.Sounds
import simulation.World
import simulation.WorldPair

fun isPlayerAlive(world: World): Boolean {
  val player = world.players[0]
  val character = world.deck.characters[player.id]!!
  return character.isAlive
}

val playerDied: (WorldPair) -> Boolean = { worlds ->
  isPlayerAlive(worlds.first) && !isPlayerAlive(worlds.second)
}

fun newGameSounds(worldList: List<World>): List<Sounds> =
    if (worldList.size != 2)
      listOf()
    else {
      val worlds = Pair(worldList.first(), worldList.last())
      filterNewSounds(worlds, listOf(
          Pair(playerDied, Sounds.girlScream)
      ))
    }