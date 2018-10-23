package generation.abstract

import generation.divide
import generation.getNodeDistance
import generation.overlaps2D
import simulation.*

fun getOverlapping(nodes: List<Node>): List<Pair<Node, Node>> {
  val result = mutableListOf<Pair<Node, Node>>()
  for (node in nodes) {
    for (other in nodes) {
      if (node === other)
        continue

      // Check if the nodes overlap and there is not already an entry from the other direction
      if (overlaps2D(node, other) && !result.any { it.first === other && it.second == node }) {
        result.add(Pair(node, other))
      }
    }
  }
  return result
}

fun areTooClose(first: Node, second: Node): Boolean {
  val distance = getNodeDistance(first, second)
  return distance < -first.radius && distance < -second.radius
}

fun gatherTriUnions(graph: Graph, node: Node): List<List<Node>> {
  val neighbors = node.neighbors(graph).toList()
  return neighbors.flatMap { other ->
    other.neighbors(graph)
        .filter { it !== node && neighbors.contains(it) }
        .map { listOf(node, other, it) }
        .toList()
  }
}

fun gatherTriUnions(graph: Graph): List<List<Node>> {
  return graph.nodes.flatMap { gatherTriUnions(graph, it) }
      .distinctBy { node -> node.sortedBy { it.hashCode() }.map { it.hashCode() }.joinToString() }
}

fun handleOverlapping(nodes: List<Node>): Graph {
  val overlapping = getOverlapping(nodes)
  val (tooClose, initialUnions) = divide(overlapping.asSequence(), { areTooClose(it.first, it.second) })
//  val nodeMap = graph.nodes.associate { node ->
//    Pair(node, groups.first.filter { it.first === node || it.second === node })
//  }
  val removedNodes1 = tooClose.map { it.first }
//      .plus(nodeMap.filter { it.value.count() > 1 &&  }.map { it.key })
      .distinct()

//  val remainingNodes = nodes.minus(removedNodes1)

  val unions = initialUnions.filter { pair -> removedNodes1.all { pair.first !== it && pair.second != it } }
//  for (node in removedNodes1) graph.removeNode(node)
//  for (pair in unions) graph.connect(pair.first, pair.second, ConnectionType.union)
  val unionConnections = unions.map {
    Connection(it.first.id, it.second.id, ConnectionType.union)
  }.toList()
  val triUnions = gatherTriUnions(Graph(nodes, unionConnections))
  val removedNodes2 = removedNodes1.plus(triUnions.map { it.first() })

  val filteredConnections = unionConnections.filter { c -> removedNodes2.none { c.contains(it) } }

  return Graph(
      nodes = nodes.minus(removedNodes2),
      connections = filteredConnections
  )
}
