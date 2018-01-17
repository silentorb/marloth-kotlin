package generation.abstract

import generation.divide
import generation.getNodeDistance
import generation.overlaps2D
import simulation.*

fun getOverlapping(nodes: List<Node>): List<Pair<Node, Node>> {
  val result = mutableListOf<Pair<Node, Node>>()
  for (node in nodes) {
    for (other in nodes) {
      if (node === other || node.isConnected(other))
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

fun gatherTriUnions(node: Node): List<List<Node>> {
  val neighbors = node.getNeighbors().toList()
  return neighbors.flatMap { other ->
    other.getNeighbors()
        .filter { it !== node && neighbors.contains(it) }
        .map { listOf(node, other, it) }
        .toList()
  }
}

fun gatherTriUnions(nodes: List<Node>): List<List<Node>> {
  return nodes.flatMap { gatherTriUnions(it) }
      .distinctBy { it.sortedBy { it.hashCode() }.map { it.hashCode() }.joinToString() }
}

fun handleOverlapping(world: AbstractWorld) {
  val overlapping = getOverlapping(world.nodes)
  val (tooClose, initialUnions) = divide(overlapping.asSequence(), { areTooClose(it.first, it.second) })
//  val nodeMap = world.nodes.associate { node ->
//    Pair(node, groups.first.filter { it.first === node || it.second === node })
//  }
  val removedNodes1 = tooClose.map { it.first }
//      .plus(nodeMap.filter { it.value.count() > 1 &&  }.map { it.key })
      .distinct()

  val remainingNodes = world.nodes.minus(removedNodes1)

//  val triUnions = gatherTriUnions(remainingNodes)

  val unions = initialUnions.filter { pair -> removedNodes1.all { pair.first !== it && pair.second != it } }
  for (node in removedNodes1) world.removeNode(node)
  for (pair in unions) world.connect(pair.first, pair.second, ConnectionType.union)

  val triUnions = gatherTriUnions(world.nodes)
  for (triUnion in triUnions) world.removeNode(triUnion.first())
}
