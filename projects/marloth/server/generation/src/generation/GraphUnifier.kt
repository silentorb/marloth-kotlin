package generation

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