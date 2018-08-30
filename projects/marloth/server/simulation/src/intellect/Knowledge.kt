package intellect

import simulation.Character
import simulation.Id
import simulation.Node
import simulation.WorldMap

data class Knowledge(
    val spiritId: Id,
    val nodes: List<Node>,
    val visibleCharacters: List<Character>,
    val world: WorldMap
) {
  val character: Character
    get() = world.characterTable[spiritId]!!
}

fun updateKnowledge(world: WorldMap, character: Character, knowledge: Knowledge?): Knowledge {
  return Knowledge(
      spiritId = character.id,
      nodes = world.meta.nodes,
      visibleCharacters = getVisibleCharacters(world, character),
      world = world
  )
}

fun getVisibleEnemies(character: Character, knowledge: Knowledge) =
    knowledge.visibleCharacters.filter { it.faction != character.faction }
