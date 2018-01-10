package generation

typealias NodeGroup = MutableList<Node>

fun getNodeDistance(first: Node, second: Node): Float =
    Math.max(0f, first.position.distance(second.position) - first.radius - second.radius)

data class NearestNodeResult(val node: Node, val distance: Float)

fun getNeighborsByDistance(node: Node, nodes: Sequence<Node>) = nodes.asSequence()
    .filter { it !== node }
    .map { NearestNodeResult(it, getNodeDistance(node, it)) }
    .sortedBy { it.distance }

fun findNearestNode(node: Node, nodes: Sequence<Node>) =
    getNeighborsByDistance(node, nodes)
        .firstOrNull()

data class NodeGap(val first: Node, val second: Node, val distance: Float)

fun findNearestGap(node: Node, nodes: Sequence<Node>): NodeGap? {
  val result = findNearestNode(node, nodes)
  if (result != null)
    return NodeGap(node, result.node, result.distance)

  return null
}

fun findShortestGap(firstGroup: Sequence<Node>, secondGroup: Sequence<Node>): NodeGap? =
    firstGroup.map { findNearestGap(it, secondGroup) }
        .filterNotNull()
        .sortedBy { it.distance }
        .firstOrNull()

fun getNeighborsToAdd(node: Node, group: NodeGroup): Sequence<Node> =
    node.getNeighbors().filter { !group.contains(it) }

fun scanChanged(changed: List<Node>, group: NodeGroup) =
    changed.asSequence()
        .map { getNeighborsToAdd(it, group) }
        .flatten()
        .distinct()

fun unifyWorld(world: AbstractWorld) {
  if (world.nodes.size < 2)
    return

  val first = world.nodes.first()
  val mainGroup = mutableListOf(first)
  val groups: MutableList<NodeGroup> = mutableListOf()
  groups.add(mainGroup)
  var changed = listOf(first)

  while (groups.size != 1 || mainGroup.size != world.nodes.size) {
    changed = scanChanged(changed, mainGroup).toList()
    if (changed.isEmpty()) {

      throw Error("Could not finish processing nodes")
    }
  }
}