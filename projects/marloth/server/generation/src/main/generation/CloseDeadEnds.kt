package generation

import mythic.spatial.Vector2
import mythic.spatial.Vector3
import org.joml.Intersectionf
import org.joml.xy

private val tunnelPadding = 2f

fun intersects(lineStart: Vector2, lineEnd: Vector2, circleCenter: Vector2, radius: Float) =
    Intersectionf.intersectLineCircle(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y, circleCenter.x, circleCenter.y, radius, Vector3())

fun nodesIntersectOther(first: Node, second: Node, nodes: Sequence<Node>) =
    nodes
        .filter { it !== first && it !== second }
        .any { intersects(first.position.xy, second.position.xy, it.position.xy, it.radius + tunnelPadding) }

fun closeDeadEnd(node: Node, world: AbstractWorld) {
  val nodes = world.nodes.asSequence()
  val nextAvailableNode = nodes
      .filter { it !== node && !it.isConnected(node) }
      .sortedBy { getNodeDistance(node, it) }
      .filter { nodesIntersectOther(node, it, nodes) }
      .firstOrNull()

//  if (nextAvailableNode != null) {
    world.connect(node, nextAvailableNode!!, ConnectionType.tunnel)
//  }
}

fun closeDeadEnds(world: AbstractWorld) {
  val deadEnds = world.nodes.filter { it.connections.size < 2 }.asSequence()
  for (node in deadEnds) closeDeadEnd(node, world)
}
