package simulation.intellect.assessment

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.physics.BulletState
import silentorb.mythic.physics.SimpleBody
import silentorb.mythic.spatial.Vector3
import simulation.entities.Character
import simulation.entities.isAlly
import simulation.entities.isEnemy
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Realm

data class CharacterMemory(
    val lastSeen: Float,
    val id: Id,
    override val position: Vector3,
    override val nearestNode: Id,
    val faction: Id,
    val targetable: Boolean
) : SimpleBody

data class Knowledge(
//    val grid: MapGrid,
    val characters: Table<CharacterMemory>
)

fun newKnowledge() =
    Knowledge(
        characters = mapOf()
    )

const val memoryLifetime: Float = 5f // In seconds

fun getKnownCharacters(realm: Realm, bulletState: BulletState, deck: Deck, lightRatings: Table<Float>, character: Id): List<Id> {
  val faction = deck.characters[character]!!.faction
  return deck.characters.keys
      .minus(character)
      .filter { id ->
        isAlly(deck.characters, faction)(id) ||
            canSee(realm, bulletState, deck, lightRatings, character)(id)
      }
}

fun updateCharacterKnowledge(world: World, character: Id, knowledge: Knowledge, lightRatings: Table<Float>, delta: Float): Table<CharacterMemory> {
  val fresh = getKnownCharacters(world.realm, world.bulletState, world.deck, lightRatings, character)
      .map { id ->
        val body = world.deck.bodies[id]!!
        val targetCharacter = world.deck.characters.getValue(id)
        Pair(id, CharacterMemory(
            lastSeen = 0f,
            id = id,
            position = body.position,
            nearestNode = body.nearestNode,
            faction = targetCharacter.faction,
            targetable = targetCharacter.isAlive
        ))
      }

  return knowledge.characters
      .mapValues { (_, it) -> it.copy(lastSeen = it.lastSeen + delta) }
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
        .filter { it.faction != character.faction && it.targetable }