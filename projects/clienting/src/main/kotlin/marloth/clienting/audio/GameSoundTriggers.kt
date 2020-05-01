package marloth.clienting.audio

import silentorb.mythic.aura.Sound
import silentorb.mythic.ent.IdSource
import silentorb.mythic.spatial.Vector3
import marloth.scenery.enums.Sounds
import marloth.scenery.enums.soundId
import simulation.main.World
import simulation.main.WorldPair

//fun isPlayerAlive(world: World): Boolean {
//  val player = world.players[0]
//  val character = world.deck.silentorb.mythic.characters[player.id]!!
//  return character.isAlive
//}
//
//val characterDied: (WorldPair) -> Boolean = { worlds ->
//  isPlayerAlive(worlds.first) && !isPlayerAlive(worlds.second)
//}

//val missileSounds: (worlds: WorldPair) -> List<NewSound> = { worlds ->
//  worlds.second.deck.missiles.filterKeys { id ->
//    worlds.first.deck.missiles[id] == null
//  }
//      .map { entry ->
//        val body = worlds.second.deck.bodies[entry.key]!!
//        NewSound(Sounds.throwWeapon, body.position)
//      }
//}

//fun newGameSounds(nextId: IdSource, worldList: List<World>): List<Sound> =
//    if (worldList.size != 2)
//      listOf()
//    else {
//      val worlds = Pair(worldList.first(), worldList.last())
//      listOf(
////          ambientSounds,
//          deathSounds
////          missileSounds
//      )
//          .flatMap { it(worlds) }
//          .map {
//            Sound(
//                id = nextId(),
//                type = soundId(it.type),
//                position = it.position
//            )
//          }
//    }
