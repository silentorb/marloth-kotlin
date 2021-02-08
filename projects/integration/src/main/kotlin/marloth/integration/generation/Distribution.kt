package marloth.integration.generation

import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Key
import silentorb.mythic.ent.scenery.gatherChildren
import silentorb.mythic.ent.scenery.getNodeTransform
import silentorb.mythic.ent.scenery.nodesWithAttribute
import silentorb.mythic.ent.scenery.removeNodesAndChildren
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

fun filterLightDistribution(graph: Graph): Graph {
  // This doesn't include all lights in the scene, only lights tagged to be potentially filtered
  val nodes = nodesWithAttribute(graph, GameAttributes.lightDistribution)
  val locationMap = nodes.map { node ->
    node to getNodeTransform(graph, node).translation()
  }
  val removed = removeClumps(graph, 10f, locationMap)
  return removeNodesAndChildren(graph, removed)
}

fun filterDistributionGroups(graph: Graph): Graph {
  return filterLightDistribution(graph)
}
