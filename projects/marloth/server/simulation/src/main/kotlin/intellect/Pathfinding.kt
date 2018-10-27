package intellect

import simulation.Node
import simulation.Realm
import simulation.getPathNeighbors

data class PathNode(
    val node: Node,
    val previous: PathNode?
)

tailrec fun unwindPath(pathNode: PathNode, path: List<Node> = listOf()): List<Node> =
    if (pathNode.previous == null)
      path
    else
      unwindPath(pathNode.previous, listOf(pathNode.node).plus(path))

tailrec fun findPath(realm: Realm, destination: Node, scanned: List<PathNode>, next: List<PathNode>): List<Node>? {
  if (next.none())
    return null

  val neighbors = next
      .flatMap { node ->
        getPathNeighbors(realm.nodeTable, realm.faces, node.node)
            .filter { n -> scanned.none { it.node == n } && next.none { it.node == n } }
            .map { PathNode(it, node) }
            .toList()
      }
      .distinctBy { it.node }

  val arrived = neighbors.filter { it.node == destination }
  if (arrived.any())
    return unwindPath(arrived.first())

  val newScanned = scanned.plus(neighbors)
  return findPath(realm, destination, newScanned, neighbors)
}

fun findPath(realm: Realm, source: Node, destination: Node): List<Node>? {
  val list = listOf(PathNode(source, null))
  return findPath(realm, destination, list, list)
}