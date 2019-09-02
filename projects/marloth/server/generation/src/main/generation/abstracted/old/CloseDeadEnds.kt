package generation.abstracted.old

import generation.abstracted.connections
import generation.abstracted.isConnected
import generation.abstracted.neighbors
import generation.misc.connectionOverlapsNeighborNodes
import generation.misc.getNodeDistance
import generation.structure.doorwayLength
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import mythic.spatial.Vector3m
import mythic.spatial.lineIntersectsCircle
import org.joml.Intersectionf
import simulation.misc.*

private const val tunnelPadding = 1f + doorwayLength * 0.5f

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
        .filter { it.id != first.id && it.id != second.id }
        .any {
          if (it.id == 67L && listOf(first.id, second.id).containsAll(listOf(52L, 62L))) {
            val k = 0
          }
          lineIntersectsCircle(first.position.xy(), second.position.xy(), it.position.xy(), it.radius + tunnelPadding) }

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
    InitialConnection(node.id, nextAvailableNode.id, ConnectionType.doorway)
  else
    null
}

fun getDeadEnds(graph: Graph) =
    graph.nodes.values.filter { connections(graph, it).size < 2 }

fun closeDeadEnds(graph: Graph): InitialConnections =
    getDeadEnds(graph)
        .drop(3) // Leaving room for a home biome, and a few dead ends are okay.
        .mapNotNull { closeDeadEnd(it, graph) }
