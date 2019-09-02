package simulation.intellect.design

import simulation.intellect.Path
import mythic.ent.Id
import simulation.misc.Realm
import simulation.misc.nodeNeighbors

data class PathNode(
    val node: Id,
    val previous: PathNode?
)

tailrec fun unwindPath(pathNode: PathNode, path: Path = listOf()): Path =
    if (pathNode.previous == null)
      path
    else
      unwindPath(pathNode.previous, listOf(pathNode.node).plus(path))

tailrec fun findPath(realm: Realm, destination: Id, scanned: List<PathNode>, next: List<PathNode>): Path? {
  if (next.none())
    return null

  val neighbors = next
      .flatMap { node ->
        nodeNeighbors(realm.graph, node.node)
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

fun findPath(realm: Realm, source: Id, destination: Id): Path? {
  val list = listOf(PathNode(source, null))
  return findPath(realm, destination, list, list)
}
