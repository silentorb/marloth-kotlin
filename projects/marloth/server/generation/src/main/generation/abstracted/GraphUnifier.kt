package generation.abstracted

import simulation.*

import generation.getNodeDistance
import mythic.spatial.Vector3

const val maxTunnelVerticalRange = 10
const val maxTunnelDot = 0.5

typealias NodeGroup = List<Node>

data class NearestNodeResult(val node: Node, val distance: Float)

fun getNeighborsByDistance(node: Node, nodes: Sequence<Node>) = nodes.asSequence()
    .filter { it !== node }
    .map { NearestNodeResult(it, getNodeDistance(node, it)) }
    .filter {it.distance <= maxTunnelLength}
    .toList()
    .sortedBy { it.distance }

data class NodeGap(val first: Node, val second: Node, val distance: Float)

typealias NodeSequenceTransform = (Node, Sequence<Node>) -> Sequence<Node>

fun findNearestGap(graph: Graph, node: Node, nodes: Sequence<Node>, filter: NodeSequenceTransform): NodeGap? {
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

private val upVector = Vector3(0f, 0f, 1f)

private val tunnelAngleFilter: NodeSequenceTransform = { node, nodes ->
  nodes.filter {
    // TODO: This math can be simplified a lot for better performance.
    val rawVector = it.position - node.position
    val initialVector = Vector3(rawVector.x, rawVector.y, 0f).normalize()
    val a = it.position - initialVector * it.radius
    val b = node.position + initialVector * it.radius
    val slopeVector = (a - b).normalize()
    val dot = Math.abs(upVector.dot(slopeVector))
//    if (dot < maxTunnelDot) {
//      if (it.id == 4L) {
//        val k = 0
//      }
//      println("${it.id} ${node.id} ${dot}")
//    }

    val c = 7L
    val d = 10L
    if ((it.id == c && node.id == d) || (it.id == d && node.id == c)) {
      val k = 0
    }
    dot < maxTunnelDot
  }
}

tailrec fun scanNodes(graph: Graph, previousChanged: List<Node>, mainGroup: List<Node>, ungrouped: List<Node>,
                      connections: InitialConnections): InitialConnections {
  val possibleChanged = scanChanged(graph, previousChanged, mainGroup).toList()
  val (changed, newConnections) = if (possibleChanged.isEmpty()) {

    val gap = findShortestGap(graph, mainGroup.asSequence(), ungrouped.asSequence(), tunnelAngleFilter)
//        ?: findShortestGap(graph, mainGroup.asSequence(), ungrouped.asSequence()) { node, nodes -> nodes }

    if (gap == null)
      Pair(listOf(), connections)
//      throw Error("Could not find gap to fill.")
    else
      Pair(listOf(gap.second), connections.plus(InitialConnection(gap.first.id, gap.second.id, ConnectionType.tunnel, FaceType.space)))
  } else {
    Pair(possibleChanged, connections)
  }

  return if (changed.none()) {
    if (ungrouped.size > 1) {
      val nextMainGroup = ungrouped.take(1)
      scanNodes(graph, nextMainGroup, nextMainGroup, ungrouped.drop(1).toList(), newConnections)
    } else
      newConnections
  } else {
    val nextMainGroup = mainGroup.plus(changed)
    if (nextMainGroup.size == graph.nodes.size)
      newConnections
    else
      scanNodes(graph, changed, nextMainGroup, ungrouped.subtract(changed).toList(), newConnections)
  }
}

fun unifyWorld(graph: Graph): InitialConnections {
  if (graph.nodes.size < 2)
    return listOf()

  val first = graph.nodes.values.first()
  val mainGroup = listOf(first)
  val ungrouped = graph.nodes.values.filter { it !== first }
  return scanNodes(graph, mainGroup, mainGroup, ungrouped, listOf())
}
