package generation.abstract

import generation.connectionOverlapsNeighborNodes
import generation.getCenter
import generation.getNodeDistance
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.lineIntersectsCircle
import org.joml.Intersectionf
import org.joml.xy
import simulation.*

private val tunnelPadding = 0f

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
      hitPoint)

  if (!hit)
    return false

  return isBetween(lineStart.x, lineEnd.x, hitPoint.x) && isBetween(lineStart.y, lineEnd.y, hitPoint.y)
}

fun nodesIntersectOther(first: Node, second: Node, nodes: Sequence<Node>) =
    nodes
        .filter { it !== first && it !== second }
        .any { lineIntersectsCircle(first.position.xy, second.position.xy, it.position.xy, it.radius + tunnelPadding) }

fun closeDeadEnd(node: Node, graph: NodeGraph) {
  val nodes = graph.nodes.asSequence()
  val neighbors = nodes
      .filter { it !== node && !it.isConnected(node) }
      .filter { !nodesIntersectOther(node, it, node.neighbors) }
      .toList()
      .sortedBy { getNodeDistance(node, it) }

  val nextAvailableNode = neighbors
      .firstOrNull()

  if (nextAvailableNode != null) {
    if (node.index == 5) {
      val k = 0
    }
    if (connectionOverlapsNeighborNodes(node.neighbors.filter { it != nextAvailableNode }.toList(), node, nextAvailableNode))
      return

    println("DE " + node.index + " " + nextAvailableNode.index)
    graph.connect(node, nextAvailableNode, ConnectionType.tunnel)
  }
}

fun closeDeadEnds(graph: NodeGraph) {
  val deadEnds = graph.nodes.filter { it.connections.size < 2 }.asSequence()
  for (node in deadEnds) closeDeadEnd(node, graph)
}
