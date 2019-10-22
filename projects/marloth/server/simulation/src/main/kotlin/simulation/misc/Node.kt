package simulation.misc

import mythic.ent.WithId
import mythic.ent.Id
import mythic.spatial.Vector3

enum class NodeAttribute {
  exit,
  home,
  room,
  stairBottom,
  stairTop,
  tunnel,
  upperLayer
}

data class Node(
    override val id: Id,
    val position: Vector3,
    val radius: Float,
    val biome: BiomeName? = null,
    val attributes: Set<NodeAttribute> = setOf()
) : WithId {

  val isRoom: Boolean
    get() = attributes.contains(NodeAttribute.room)
}

fun nodeNeighbors(graph: Graph, node: Id) =
    graph.connections
        .filter { it.contains(node) }
        .flatMap { it.nodes.minus(node) }
        .distinct()
