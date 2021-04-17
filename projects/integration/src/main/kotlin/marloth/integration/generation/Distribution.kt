package marloth.integration.generation

import silentorb.mythic.ent.Entry
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.scenery.*
import silentorb.mythic.scenery.SceneProperties
import silentorb.mythic.spatial.Vector3
import simulation.misc.GameAttributes

// This is a primitive method but relatively fast and works for cases where even distribution is not needed
tailrec fun removeClumps(graph: Graph, range: Float, nodes: List<Pair<Key, Vector3>>, removed: List<Key> = listOf()): List<Key> =
    if (nodes.none())
      removed
    else {
      val (next, location) = nodes.first()
      val remaining = nodes.drop(1)
      val tooClose = remaining
          .filter { it.second.distance(location) < range }

      removeClumps(graph, range, remaining - tooClose, removed + tooClose.map { it.first })
    }

fun filterLightDistribution(library: ExpansionLibrary, graph: Graph, slots: SlotMap): Pair<Graph, Set<Key>> {
  // This doesn't include all lights in the scene, only lights tagged to be potentially filtered
  val lights = nodesWithAttribute(graph, GameAttributes.lightDistribution)
  val locationMap = lights.map { node ->
    node to getNodeTransform(graph, node).translation()
  } + slots.map { it.key to it.value.transform.translation() }

  val removed = removeClumps(graph, 10f, locationMap)
  val usedSlots = slots - removed
  val afterRemoved = removeNodesAndChildren(graph, removed.intersect(lights))
  val withNewLights = usedSlots.flatMap { (_, slot) ->
    val key = "light${slot.transform.hashCode()}"
    val entries = listOf(
        Entry(key, SceneProperties.type, "lamppost"),
        Entry(key, SceneProperties.translation, slot.transform.translation()),
    )
    expandInstances(library, entries)
  }
  return Pair(afterRemoved + withNewLights, usedSlots.keys)
}

fun filterDistributionGroups(library: ExpansionLibrary, graph: Graph): Graph {
  val slots = gatherSlots(graph)
  val (nextGraph, nextSlots) = filterLightDistribution(library, graph, slots)
  return nextGraph
}
