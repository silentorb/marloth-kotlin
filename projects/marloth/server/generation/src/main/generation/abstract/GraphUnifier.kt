package generation.abstract

import generation.getNodeDistance
import simulation.ConnectionType
import simulation.Node
import simulation.NodeGraph

typealias NodeGroup = List<Node>

data class NearestNodeResult(val node: Node, val distance: Float)

fun getNeighborsByDistance(node: Node, nodes: Sequence<Node>) = nodes.asSequence()
    .filter { it !== node }
    .map { NearestNodeResult(it, getNodeDistance(node, it)) }
    .sortedBy { it.distance }

//fun findNearestNode(node: Node, nodes: Sequence<Node>) =
//    getNeighborsByDistance(node, nodes)
//        .firstOrNull()

data class NodeGap(val first: Node, val second: Node, val distance: Float)

fun findNearestGap(node: Node, nodes: Sequence<Node>): NodeGap? {
  val result = getNeighborsByDistance(node, nodes)
      .filter { !it.node.isConnected(node) }
      .firstOrNull()

  if (result != null)
    return NodeGap(node, result.node, result.distance)

  return null
}

fun findShortestGap(firstGroup: Sequence<Node>, secondGroup: Sequence<Node>): NodeGap? =
    firstGroup.map { findNearestGap(it, secondGroup) }
        .filterNotNull()
        .sortedBy { it.distance }
        .firstOrNull()

fun getNeighborsToAdd(node: Node, group: NodeGroup): Sequence<Node> =
    node.getNeighbors().filter { !group.contains(it) }

fun scanChanged(changed: List<Node>, group: NodeGroup) =
    changed.asSequence()
        .map { getNeighborsToAdd(it, group) }
        .flatten()
        .distinct()

tailrec fun scanNodes(previousChanged: List<Node>, mainGroup: List<Node>, outerGroup: List<Node>, graph: NodeGraph) {
  val possibleChanged = scanChanged(previousChanged, mainGroup).toList()
  val changed = if (possibleChanged.isEmpty()) {
    val gap = findShortestGap(mainGroup.asSequence(), outerGroup.asSequence())
    if (gap == null)
      throw Error("Could not find gap to fill.")

    graph.connect(gap.first, gap.second, ConnectionType.tunnel)
    listOf(gap.second)
  } else {
    possibleChanged
  }

  val nextMainGroup = mainGroup.plus(changed)
  if (nextMainGroup.size != graph.nodes.size)
    return scanNodes(changed, nextMainGroup, outerGroup.subtract(changed).toList(), graph)
}

fun unifyWorld(graph: NodeGraph) {
  if (graph.nodes.size < 2)
    return

  val first = graph.nodes.first()
  val mainGroup = listOf(first)
  val outerGroup = graph.nodes.filter { it !== first }
  scanNodes(mainGroup, mainGroup, outerGroup, graph)
}