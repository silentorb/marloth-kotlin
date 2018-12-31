package intellect.acessment

import mythic.ent.Entity
import simulation.Character
import mythic.ent.Id
import mythic.ent.Table
import mythic.spatial.Vector3
import physics.SimpleBody
import simulation.Biome
import simulation.World

data class CharacterMemory(
    val lastSeen: Float,
    override val id: Id,
    override val position: Vector3,
    override val node: Id,
    val faction: Id,
    val targetable: Boolean
) : Entity, SimpleBody

data class Knowledge(
    val spiritId: Id,
    val nodes: List<Id>,
    val characters: Table<CharacterMemory>
)

const val memoryLifetime: Float = 5f // In seconds

fun character(world: World, knowledge: Knowledge): Character =
    world.deck.characters[knowledge.spiritId]!!

fun updateCharacterKnowledge(world: World, character: Character, knowledge: Knowledge, delta: Float): Table<CharacterMemory> {
  val fresh = getVisibleCharacters(world, character).associate { c ->
    val body = world.deck.bodies[c.id]!!
    val node = world.realm.nodeTable[body.node]
    Pair(c.id, CharacterMemory(
        lastSeen = 0f,
        id = c.id,
        position = body.position,
        node = body.node,
        faction = c.faction,
        targetable = c.isAlive && (node == null || node.biome != Biome.home)
    ))
  }

  return knowledge.characters
      .mapValues { (_, it) -> it.copy(lastSeen = it.lastSeen + delta) }
      .filter { (_, it) -> it.lastSeen < memoryLifetime }
      .plus(fresh)
}

fun newKnowledge(world: World, character: Character): Knowledge =
    Knowledge(
        spiritId = character.id,
        nodes = world.realm.nodeList.map { it.id },
        characters = mapOf()
    )

fun updateKnowledge(world: World, character: Character, knowledge: Knowledge, delta: Float): Knowledge {
  return knowledge.copy(
      characters = updateCharacterKnowledge(world, character, knowledge, delta)
  )
}

fun getVisibleEnemies(character: Character, knowledge: Knowledge): List<CharacterMemory> =
    knowledge.characters.values
        .filter { it.faction != character.faction && it.targetable }
