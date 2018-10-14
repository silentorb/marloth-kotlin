package intellect

import simulation.Character
import simulation.Id
import simulation.Node
import simulation.World

data class Knowledge(
    val spiritId: Id,
    val nodes: List<Node>,
    val visibleCharacters: List<Character>,
    val world: World
) {
  val character: Character
    get() = world.characterTable[spiritId]!!
}

fun updateKnowledge(world: World, character: Character, knowledge: Knowledge?): Knowledge {
  return Knowledge(
      spiritId = character.id,
      nodes = world.realm.nodes,
      visibleCharacters = getVisibleCharacters(world, character),
      world = world
  )
}

fun getVisibleEnemies(character: Character, knowledge: Knowledge) =
    knowledge.visibleCharacters.filter { it.faction != character.faction }
