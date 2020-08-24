package simulation.intellect.assessment

import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.physics.BulletState
import silentorb.mythic.spatial.Vector3
import simulation.characters.Character
import simulation.characters.isAlly
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Realm

data class CharacterMemory(
    val lastSeen: Float,
    val id: Id,
    val lastPosition: Vector3,
    val position: Vector3?,
    val faction: Id,
    val targetable: Boolean
)

data class Knowledge(
//    val grid: MapGrid,
    val characters: Table<CharacterMemory>
)

fun newKnowledge() =
    Knowledge(
        characters = mapOf()
    )

const val memoryLifetime: Float = 5f // In seconds

fun getKnownCharacters(world: World, lightRatings: Table<Float>, character: Id): List<Id> {
  val deck = world.deck
  return if (getDebugBoolean("BLIND_AI"))
    listOf()
  else {
    val faction = deck.characters[character]!!.faction
    deck.characters.keys
        .minus(character)
        .filter { id ->
          isAlly(deck.characters, faction)(id) ||
              canSee(world, lightRatings, character)(id)
        }
  }
}

fun updateCharacterKnowledge(world: World, character: Id, knowledge: Knowledge, lightRatings: Table<Float>, delta: Float): Table<CharacterMemory> {
  val fresh = getKnownCharacters(world, lightRatings, character)
      .map { id ->
        val body = world.deck.bodies[id]!!
        val targetCharacter = world.deck.characters.getValue(id)
        Pair(id, CharacterMemory(
            lastSeen = 0f,
            id = id,
            lastPosition = body.position,
            position = body.position,
            faction = targetCharacter.faction,
            targetable = targetCharacter.isAlive
        ))
      }

  return knowledge.characters
      .mapValues { (_, it) ->
        it.copy(
            lastSeen = it.lastSeen + delta,
            position = null
        )
      }
      .filter { (_, it) -> it.lastSeen < memoryLifetime }
      .plus(fresh)
}

fun newKnowledge(world: World): Knowledge =
    Knowledge(
//        grid = world.realm.grid,
        characters = mapOf()
    )

fun updateKnowledge(world: World, lightRatings: Table<Float>, delta: Float): (Id, Knowledge) -> Knowledge =
    { character, knowledge ->
      knowledge.copy(
          characters = updateCharacterKnowledge(world, character, knowledge, lightRatings, delta)
      )
    }

fun getVisibleEnemies(character: Character, knowledge: Knowledge): List<CharacterMemory> =
    knowledge.characters.values
        .filter { false }
//        .filter { it.faction != character.faction && it.targetable }
