package marloth.integration.generation

import generation.architecture.engine.GenerationConfig
import marloth.clienting.editing.biomeIds
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.IdSource
import silentorb.mythic.ent.getNodeValue
import silentorb.mythic.ent.getNodeValues
import silentorb.mythic.randomly.Dice
import silentorb.mythic.scenery.SceneProperties
import simulation.main.NewHand
import simulation.misc.GameProperties

tailrec fun getNodeBiomesWithInheritance(graph: Graph, node: String, accumulator: List<String> = listOf()): List<String> {
  val biomes = accumulator
      .plus(getNodeValues(graph, node, GameProperties.biome))
      .distinct()

  val parent = getNodeValue<String>(graph, node, SceneProperties.parent)
  return if (parent == null)
    biomes
  else
    getNodeBiomesWithInheritance(graph, parent, accumulator)
}

fun slotToHands(config: GenerationConfig, nextId: IdSource, dice: Dice, slot: Slot, filter: PropFilter): List<NewHand> {
  val slotAttribute = mapSlotType(slot)
  val definitions = filterPropGraphs(config) {
    it.contains(slotAttribute)
        && filter(it)
        && slot.attributes.containsAll(biomeIds.intersect(it))
  }
  return if (definitions.none())
    listOf()
  else {
    val definition = dice.takeOne(definitions)
    graphToHands(config.resourceInfo, nextId, definition, slot.transform)
  }
}
