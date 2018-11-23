package generation.abstracted
import simulation.*

import generation.getNodeDistance

typealias NodeGroup = List<Node>

data class NearestNodeResult(val node: Node, val distance: Float)

fun getNeighborsByDistance(node: Node, nodes: Sequence<Node>) = nodes.asSequence()
    .filter { it !== node }
    .map { NearestNodeResult(it, getNodeDistance(node, it)) }
    .toList()
    .sortedBy { it.distance }

//fun findNearestNode(node: Node, nodes: Sequence<Node>) =
//    getNeighborsByDistance(node, nodes)
//        .firstOrNull()

data class NodeGap(val first: Node, val second: Node, val distance: Float)

fun findNearestGap(graph: Graph, node: Node, nodes: Sequence<Node>): NodeGap? {
  val neighbors = getNeighborsByDistance(node, nodes)
      .filter { !isConnected(graph,it.node, node) }
  val nearest = neighbors
      .firstOrNull()

  return if (nearest != null)
    NodeGap(node, nearest.node, nearest.distance)
  else
    null
}

fun findShortestGap(graph: Graph, firstGroup: Sequence<Node>, secondGroup: Sequence<Node>): NodeGap? =
    firstGroup.map { findNearestGap(graph, it, secondGroup) }
        .filterNotNull()
        .sortedBy { it.distance }
        .firstOrNull()

fun getNeighborsToAdd(graph: Graph, node: Node, group: NodeGroup): Sequence<Node> =
    neighbors(graph, node).filter { !group.contains(it) }

fun scanChanged(graph: Graph, changed: List<Node>, group: NodeGroup) =
    changed.asSequence()
        .map { getNeighborsToAdd(graph, it, group) }
        .flatten()
        .distinct()

tailrec fun scanNodes(graph: Graph, previousChanged: List<Node>, mainGroup: List<Node>, outerGroup: List<Node>,
                      connections: InitialConnections): InitialConnections {
  val possibleChanged = scanChanged(graph, previousChanged, mainGroup).toList()
  val (changed, newConnections) = if (possibleChanged.isEmpty()) {
    val gap = findShortestGap(graph, mainGroup.asSequence(), outerGroup.asSequence())
    if (gap == null)
      throw Error("Could not find gap to fill.")

//    println("" + gap.first.index + " " + gap.second.index)
    Pair(listOf(gap.second), connections.plus(InitialConnection(gap.first.id, gap.second.id, ConnectionType.tunnel, FaceType.space)))
  } else {
    Pair(possibleChanged, connections)
  }

  val nextMainGroup = mainGroup.plus(changed)
  return if (nextMainGroup.size == graph.nodes.size)
    newConnections
  else
    scanNodes(graph, changed, nextMainGroup, outerGroup.subtract(changed).toList(), newConnections)
}

fun unifyWorld(graph: Graph): InitialConnections {
  if (graph.nodes.size < 2)
    return listOf()

  val first = graph.nodes.values.first()
  val mainGroup = listOf(first)
  val outerGroup = graph.nodes.values.filter { it !== first }
  return scanNodes(graph, mainGroup, mainGroup, outerGroup, listOf())
}