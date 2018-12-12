package generation.abstracted

import generation.connectionOverlapsNeighborNodes
import generation.getNodeDistance
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.Vector3m
import mythic.spatial.lineIntersectsCircle
import org.joml.Intersectionf
import simulation.*

private const val tunnelPadding = 0f

fun isBetween(first: Float, second: Float, middle: Float) =
    if (first < second)
      middle >= first && middle <= second
    else
      middle >= second && middle <= first

fun intersects(lineStart: Vector2, lineEnd: Vector2, circleCenter: Vector2, radius: Float): Boolean {
  val hitPoint = Vector3()
  val hit = Intersectionf.intersectLineCircle(
      lineStart.x, lineStart.y,
      lineEnd.x, lineEnd.y,
      circleCenter.x, circleCenter.y, radius,
      Vector3m(hitPoint))

  if (!hit)
    return false

  return isBetween(lineStart.x, lineEnd.x, hitPoint.x) && isBetween(lineStart.y, lineEnd.y, hitPoint.y)
}

fun nodesIntersectOther(first: Node, second: Node, nodes: Sequence<Node>) =
    nodes
        .filter { it !== first && it !== second }
        .any { lineIntersectsCircle(first.position.xy(), second.position.xy(), it.position.xy(), it.radius + tunnelPadding) }

fun closeDeadEnd(node: Node, graph: Graph): InitialConnection? {
  val nodes = graph.nodes.values.asSequence()
  val neighbors = nodes
      .filter { it !== node && !isConnected(graph, it, node) }
      .filter { !nodesIntersectOther(node, it, neighbors(graph, node)) }
      .toList()
      .sortedBy { getNodeDistance(node, it) }

  val nextAvailableNode = neighbors
      .firstOrNull()

  return if (nextAvailableNode != null &&
      !connectionOverlapsNeighborNodes(neighbors(graph, node).filter { it != nextAvailableNode }.toList(), node, nextAvailableNode))
    InitialConnection(node.id, nextAvailableNode.id, ConnectionType.tunnel, FaceType.space)
  else
    null
}

fun getDeadEnds(graph: Graph) =
    graph.nodes.values.filter { connections(graph, it).size < 2 }

fun closeDeadEnds(graph: Graph): InitialConnections =
    getDeadEnds(graph)
        .drop(2) // Leaving room for a home biome, and a few dead ends are okay.
        .mapNotNull { closeDeadEnd(it, graph) }