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


fun filterDistributionGroups(library: ExpansionLibrary, graph: Graph): Graph {
  val slots = gatherSlots(graph)
  return filterLightDistribution(library, graph, slots)
}
