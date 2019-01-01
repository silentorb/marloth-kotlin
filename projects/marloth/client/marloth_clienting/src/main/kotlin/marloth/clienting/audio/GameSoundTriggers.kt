package marloth.clienting.audio

import mythic.aura.Sound
import mythic.ent.IdSource
import mythic.spatial.Vector3
import scenery.Sounds
import scenery.soundId
import simulation.World
import simulation.WorldPair

//fun isPlayerAlive(world: World): Boolean {
//  val player = world.players[0]
//  val character = world.deck.characters[player.id]!!
//  return character.isAlive
//}
//
//val characterDied: (WorldPair) -> Boolean = { worlds ->
//  isPlayerAlive(worlds.first) && !isPlayerAlive(worlds.second)
//}

data class NewSound(
    val type: Sounds,
    val position: Vector3
)

val deathSounds: (worlds: WorldPair) -> List<NewSound> = { worlds ->
  worlds.second.deck.characters.filter { (key, value) ->
    val previous = worlds.first.deck.characters[key]
    previous != null && previous.isAlive && !value.isAlive
  }
      .map {
        val body = worlds.second.deck.bodies[it.key]!!
        NewSound(
            type = it.value.definition.deathSound,
            position = body.position
        )
      }
}

val ambientSounds: (worlds: WorldPair) -> List<NewSound> = { worlds ->
  worlds.second.deck.ambientSounds.filter { (_, emitter) ->
    emitter.sound != null && worlds.first.deck.ambientSounds[emitter.id]?.sound == null
  }
      .map { (_, emitter) ->
        val body = worlds.second.deck.bodies[emitter.id]!!
        NewSound(emitter.sound!!, body.position)
      }
}

val missileSounds: (worlds: WorldPair) -> List<NewSound> = { worlds ->
  worlds.second.deck.missiles.filterKeys { id ->
    worlds.first.deck.missiles[id] == null
  }
      .map { entry ->
        val body = worlds.second.deck.bodies[entry.key]!!
        NewSound(Sounds.throwWeapon, body.position)
      }
}

fun newGameSounds(nextId: IdSource, worldList: List<World>): List<Sound> =
    if (worldList.size != 2)
      listOf()
    else {
      val worlds = Pair(worldList.first(), worldList.last())
      listOf(
          ambientSounds,
          deathSounds,
          missileSounds
      )
          .flatMap { it(worlds) }
          .map {
            Sound(
                id = nextId(),
                type = soundId(it.type),
                position = it.position
            )
          }
    }