package generation.abstracted

import simulation.*

import generation.getNodeDistance

const val maxTunnelVerticalRange = 10

typealias NodeGroup = List<Node>

data class NearestNodeResult(val node: Node, val distance: Float)

fun getNeighborsByDistance(node: Node, nodes: Sequence<Node>) = nodes.asSequence()
    .filter { it !== node }
    .map { NearestNodeResult(it, getNodeDistance(node, it)) }
    .toList()
    .sortedBy { it.distance }

data class NodeGap(val first: Node, val second: Node, val distance: Float)

typealias NodeSequenceTransform = (Node, Sequence<Node>) -> Sequence<Node>

fun findNearestGap(graph: Graph, node: Node, nodes: Sequence<Node>, filter: NodeSequenceTransform): NodeGap? {
//  val withinHeightRange = nodes
//      .filter { Math.abs(it.position.z - node.position.z) < maxTunnelVerticalRange }

  val filteredNodes = filter(node, nodes)

  val neighbors = getNeighborsByDistance(node, filteredNodes)
      .filter { !isConnected(graph, it.node, node) }

  val nearest = neighbors
      .firstOrNull()

  return if (nearest != null)
    NodeGap(node, nearest.node, nearest.distance)
  else
    null
}

fun findShortestGap(graph: Graph, firstGroup: Sequence<Node>, secondGroup: Sequence<Node>,
                    filter: NodeSequenceTransform): NodeGap? =
    firstGroup.map { findNearestGap(graph, it, secondGroup, filter) }
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

val verticalFilter: NodeSequenceTransform = { node, nodes ->
  nodes.filter { Math.abs(it.position.z - node.position.z) < maxTunnelVerticalRange }
}

tailrec fun scanNodes(graph: Graph, previousChanged: List<Node>, mainGroup: List<Node>, outerGroup: List<Node>,
                      connections: InitialConnections): InitialConnections {
  val possibleChanged = scanChanged(graph, previousChanged, mainGroup).toList()
  val (changed, newConnections) = if (possibleChanged.isEmpty()) {

    val gap = findShortestGap(graph, mainGroup.asSequence(), outerGroup.asSequence(), verticalFilter)
        ?: findShortestGap(graph, mainGroup.asSequence(), outerGroup.asSequence()) { node, nodes -> nodes }

    if (gap == null)
      throw Error("Could not find gap to fill.")

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