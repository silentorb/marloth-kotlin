package intellect

import simulation.Character
import mythic.ent.Id
import simulation.Node
import simulation.World

data class Knowledge(
    val spiritId: Id,
    val nodes: List<Id>,
    val visibleCharacters: List<Id>,
    val world: World
) {
  val character: Character
    get() = world.characterTable[spiritId]!!
}

data class SpiritKnowledge(
    val spiritId: Id,
    val nodes: List<Id>,
    val visibleCharacters: List<Id>
)

fun convertKnowledge(world: World, input: SpiritKnowledge) =
    Knowledge(
        spiritId = input.spiritId,
        nodes = input.nodes,
        visibleCharacters = input.visibleCharacters,
        world = world
    )

fun updateKnowledge(world: World, character: Character): SpiritKnowledge {
  return SpiritKnowledge(
      spiritId = character.id,
      nodes = world.realm.nodeList.map { it.id },
      visibleCharacters = getVisibleCharacters(world, character).map { it.id }
  )
}

fun getVisibleEnemies(character: Character, knowledge: Knowledge) =
    knowledge.visibleCharacters
        .map {knowledge.world.characterTable[it]!!}
        .filter { it.faction != character.faction }
