package generation.abstract

import generation.getNodeDistance
import generation.lineIntersectsCircle
import mythic.spatial.Vector2
import mythic.spatial.Vector3
import org.joml.Intersectionf
import org.joml.xy

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

fun closeDeadEnd(node: Node, world: AbstractWorld) {
  val nodes = world.nodes.asSequence()
  val nextAvailableNode = nodes
      .filter { it !== node && !it.isConnected(node) }
      .sortedBy { getNodeDistance(node, it) }
      .filter { !nodesIntersectOther(node, it, node.getNeighbors()) }
      .firstOrNull()

  if (nextAvailableNode != null) {
    world.connect(node, nextAvailableNode, ConnectionType.tunnel)
  }
}

fun closeDeadEnds(world: AbstractWorld) {
  val deadEnds = world.nodes.filter { it.connections.size < 2 }.asSequence()
  for (node in deadEnds) closeDeadEnd(node, world)
}
