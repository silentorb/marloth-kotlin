package intellect

import simulation.Character
import simulation.Node
import simulation.WorldMap

data class Knowledge(
    val character: Character,
    val nodes: List<Node>,
    val visibleCharacters: List<Character>
)

fun updateKnowledge(world: WorldMap, character: Character, knowledge: Knowledge): Knowledge {
  return Knowledge(
      character = character,
      nodes = world.meta.nodes,
      visibleCharacters = getVisibleCharacters(world, character)
  )
}

fun getVisibleEnemies(character: Character, knowledge: Knowledge) =
    knowledge.visibleCharacters.filter { it.faction != character.faction }
