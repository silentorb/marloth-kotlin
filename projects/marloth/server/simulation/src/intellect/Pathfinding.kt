package intellect

import simulation.Node
import simulation.NodeType

data class PathNode(
    val node: Node,
    val previous: PathNode?
)

tailrec fun unwindPath(pathNode: PathNode, path: List<Node> = listOf()): List<Node> =
    if (pathNode.previous == null)
      path
    else
      unwindPath(pathNode.previous, path)

fun getPathNeighbors(node: Node) =
    node.neighbors.filter { it.type != NodeType.space }

tailrec fun findPath(source: Node, destination: Node, scanned: List<PathNode>, next: List<PathNode>): List<Node>? {
  if (next.none())
    return null

  val neighbors = next
      .flatMap { node ->
        getPathNeighbors(node.node)
            .filter { n -> scanned.none { it.node == n } && next.none { it.node == n } }
            .map { PathNode(it, node) }
            .toList()
      }
      .distinctBy { it.node }

  val arrived = neighbors.filter { it.node == destination }
  if (arrived.any())
    return unwindPath(arrived.first())

  val newScanned = scanned.plus(neighbors)
  return findPath(source, destination, newScanned, neighbors)
}

fun findPath(source: Node, destination: Node): List<Node>? {
  val list = listOf(PathNode(source, null))
  return findPath(source, destination, list, list)
}