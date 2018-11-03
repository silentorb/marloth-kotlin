import mythic.ent.Entity

typealias Neighbors<T> = (T) -> Collection<T>
typealias GraphFinished<T> = (List<T>) -> List<T>?

tailrec fun <T : Entity> gatherNodes(scanned: List<T>, next: List<T> = scanned, gatherNeighbors: Neighbors<T>): List<T> {
  if (next.none())
    return scanned

  val neighbors = next
      .flatMap { node ->
        gatherNeighbors(node)
            .filter { n -> scanned.none { it.id == n.id } && next.none { it.id == n.id } }
            .toList()
      }
      .distinctBy { it.id }

  val newScanned = scanned.plus(neighbors)
  return gatherNodes(newScanned, neighbors, gatherNeighbors)
}
