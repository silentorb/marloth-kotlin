package generation

fun getNodeDistance(first: Node, second: Node): Float =
    Math.max(0f, first.position.distance(second.position) - first.radius - second.radius)

fun findNearestNode(node: Node, world: AbstractWorld): Node? {
  if (world.nodes.size < 2)
    return null

  return world.nodes.asSequence()
      .filter { it !== node }
      .sortedBy { getNodeDistance(node, it) }
      .firstOrNull()
}

class GraphUnifier(val world: AbstractWorld) {
  val unifiedNodes: MutableList<Node> = mutableListOf()
  val changed: MutableList<Node> = mutableListOf()

  private fun scanNode(node: Node): Boolean {
    var changed = false
    for (other in node.getNeighbors()) {
      if (!unifiedNodes.contains(other)) {
        unifiedNodes.add(other)
        changed = true
      }
    }
    return changed
  }

  fun unify() {
    if (world.nodes.size < 2)
      return

    scanNode(world.nodes.first())
//    while (unifiedNodes.size != world.nodes.size) {
//
//    }
  }
}