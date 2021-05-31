package marloth.integration.generation

import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.scenery.*
import silentorb.mythic.scenery.SceneProperties
import simulation.misc.GameAttributes

fun filterLightDistribution(library: ExpansionLibrary, graph: Graph, slots: SlotMap): Graph {
  // This doesn't include all lights in the scene, only lights tagged to be potentially filtered
  val lights = nodesWithAttribute(graph, GameAttributes.lightDistribution)
  val locationMap = lights.map { node ->
    node to getAbsoluteNodeTransform(graph, node).translation()
  } + slots.map { it.key to it.value.transform.translation() }

  val notUsed = removeClumps(graph, 10f, locationMap)
  val usedSlots = slots - notUsed
  val removed = (notUsed - slots.keys) + usedSlots.keys // Invert which slot nodes are removed from the graph
  val afterRemoved = removeNodesAndChildren(graph, removed)
  val withNewLights = usedSlots.flatMap { (_, slot) ->
    val key = "light${slot.transform.hashCode()}"
    val entries = listOf(
        Entry(key, SceneProperties.type, "lamppost"),
        Entry(key, SceneProperties.translation, slot.transform.translation()),
    )
    expandInstances(library, entries)
  }
  return afterRemoved + withNewLights
}
