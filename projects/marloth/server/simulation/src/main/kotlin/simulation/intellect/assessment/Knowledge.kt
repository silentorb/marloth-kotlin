package simulation.intellect.assessment

import simulation.entities.Character
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.physics.SimpleBody
import simulation.main.World
import simulation.misc.MapGrid

data class CharacterMemory(
    val lastSeen: Float,
    val id: Id,
    override val position: Vector3,
    override val nearestNode: Id,
    val faction: Id,
    val targetable: Boolean
) : SimpleBody

data class Knowledge(
    val grid: MapGrid,
    val characters: Table<CharacterMemory>
)

const val memoryLifetime: Float = 5f // In seconds

//fun character(world: World, knowledge: Knowledge): Character =
//    world.deck.characters[knowledge.spiritId]!!

fun updateCharacterKnowledge(world: World, character: Id, knowledge: Knowledge, delta: Float): Table<CharacterMemory> {
  val fresh = getVisibleCharacters(world.bulletState, world.deck, character)
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
        grid = world.realm.grid,
        characters = mapOf()
    )

fun updateKnowledge(world: World, character: Id, knowledge: Knowledge, delta: Float): Knowledge {
  return knowledge.copy(
      characters = updateCharacterKnowledge(world, character, knowledge, delta)
  )
}

fun getVisibleEnemies(character: Character, knowledge: Knowledge): List<CharacterMemory> =
    knowledge.characters.values
        .filter { it.faction != character.faction && it.targetable }