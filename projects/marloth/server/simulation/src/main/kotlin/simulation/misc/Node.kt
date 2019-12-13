package simulation.misc

import silentorb.mythic.ent.WithId
import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector3

enum class NodeAttribute {
  categoryCommon,
  categoryDiagonal,
  lockedRotation,
  exit,
  home,
  fullFloor, // The majority of the floor is contiguous and available for placing objects
  tunnel
}

data class Node(
    override val id: Id,
    val position: Vector3,
    val radius: Float,
    val biome: BiomeName? = null,
    val attributes: Set<NodeAttribute> = setOf()
) : WithId {

  val isRoom: Boolean
    get() = attributes.contains(NodeAttribute.fullFloor)
}

fun nodeNeighbors(graph: Graph, node: Id) =
    graph.connections
        .filter { it.contains(node) }
        .flatMap { it.nodes.minus(node) }
        .distinct()
