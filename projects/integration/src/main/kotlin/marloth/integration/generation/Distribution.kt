package marloth.integration.generation

import silentorb.mythic.ent.Key
import silentorb.mythic.spatial.Vector3

// This is a primitive method but relatively fast and works for cases where even distribution is not needed
tailrec fun removeClumps(range: Float, nodes: List<Vector3>, removed: List<Vector3> = listOf()): List<Vector3> =
    if (nodes.none())
      removed
    else {
      val location = nodes.first()
      val remaining = nodes.drop(1)
      val tooClose = remaining
          .filter { it.distance(location) < range }

      removeClumps(range, remaining - tooClose, removed + tooClose)
    }

//fun filterDistributionGroups(library: ExpansionLibrary, props: PropMap, graph: Graph): Graph {
//  val slots = gatherSlots(graph, setOf(GraphSlotTypes.ground, GraphSlotTypes.wall))
//  return distributeLights(library, graph, slots)
//}
