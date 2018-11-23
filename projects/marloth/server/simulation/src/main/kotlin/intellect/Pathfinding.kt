package intellect

import mythic.ent.Id
import simulation.Node
import simulation.Realm
import simulation.getPathNeighbors

data class PathNode(
    val node: Node,
    val previous: PathNode?
)

tailrec fun unwindPath(pathNode: PathNode, path: Path = listOf()): Path =
    if (pathNode.previous == null)
      path
    else
      unwindPath(pathNode.previous, listOf(pathNode.node.id).plus(path))

tailrec fun findPath(realm: Realm, destination: Id, scanned: List<PathNode>, next: List<PathNode>): Path? {
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

  val arrived = neighbors.filter { it.node.id == destination }
  if (arrived.any())
    return unwindPath(arrived.first())

  val newScanned = scanned.plus(neighbors)
  return findPath(realm, destination, newScanned, neighbors)
}

fun findPath(realm: Realm, source: Node, destination: Id): Path? {
  val list = listOf(PathNode(source, null))
  return findPath(realm, destination, list, list)
}