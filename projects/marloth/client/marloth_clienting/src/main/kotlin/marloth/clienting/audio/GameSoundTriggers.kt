package marloth.clienting.audio

import mythic.aura.Sound
import mythic.ent.IdSource
import mythic.spatial.Vector3
import marloth.scenery.enums.Sounds
import marloth.scenery.enums.soundId
import simulation.main.World
import simulation.main.WorldPair

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
  worlds.second.deck.ambientSounds.filter { (id, emitter) ->
    emitter.sound != null && worlds.first.deck.ambientSounds[id]?.sound == null
  }
      .map { (id, emitter) ->
        val body = worlds.second.deck.bodies[id]!!
        NewSound(emitter.sound!!, body.position)
      }
}

//val missileSounds: (worlds: WorldPair) -> List<NewSound> = { worlds ->
//  worlds.second.deck.missiles.filterKeys { id ->
//    worlds.first.deck.missiles[id] == null
//  }
//      .map { entry ->
//        val body = worlds.second.deck.bodies[entry.key]!!
//        NewSound(Sounds.throwWeapon, body.position)
//      }
//}

fun newGameSounds(nextId: IdSource, worldList: List<World>): List<Sound> =
    if (worldList.size != 2)
      listOf()
    else {
      val worlds = Pair(worldList.first(), worldList.last())
      listOf(
          ambientSounds,
          deathSounds
//          missileSounds
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
