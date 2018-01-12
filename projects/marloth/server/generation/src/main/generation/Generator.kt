package generation

import mythic.spatial.Vector3
import org.joml.minus
import org.joml.plus
import randomly.Dice

fun getOverlapping(nodes: List<Node>): List<Pair<Node, Node>> {
  val result = mutableListOf<Pair<Node, Node>>()
  for (node in nodes) {
    for (other in nodes) {
      if (node === other || node.isConnected(other))
        continue

      // Check if the nodes overlap and there is not already an entry from the other direction
      if (overlaps2D(node, other) && !result.any { it.first === other && it.second == node }) {
        result.add(Pair(node, other))
      }
    }
  }
  return result
}

fun <T> divide(sequence: Sequence<T>, filter: (T) -> Boolean) =
    Pair(sequence.filter(filter), sequence.filter { !filter(it) })

fun areTooClose(first: Node, second: Node): Boolean {
  val distance = getNodeDistance(first, second)
  return distance < -first.radius && distance < -second.radius
}

fun handleOverlapping(world: AbstractWorld) {
  val overlapping = getOverlapping(world.nodes)
  val groups = divide(overlapping.asSequence(), { areTooClose(it.first, it.second) })

  for (pair in groups.second) world.connect(pair.first, pair.second, ConnectionType.union)

  for (pair in groups.first) world.removeNode(pair.first)
}

//class Generator(
//    val abstractWorld: AbstractWorld,
//    val structureWorld: StructureWorld,
//    val dice: Dice) {
//
//}

fun createNode(abstractWorld: AbstractWorld, dice: Dice): Node {
  val radius = dice.getFloat(5f, 10f)
  val start = abstractWorld.boundary.start + radius
  val end = abstractWorld.boundary.end - radius
  val node = Node(
      Vector3(dice.getFloat(start.x, end.x), dice.getFloat(start.y, end.y), 0f),
      radius
  )
  abstractWorld.nodes.add(node)
  return node
}

fun createNodes(count: Int, abstractWorld: AbstractWorld, dice: Dice) {
  for (i in 0..count) {
    createNode(abstractWorld, dice)
  }
}

fun generateAbstract(abstractWorld: AbstractWorld, dice: Dice) {
  createNodes(20, abstractWorld, dice)
  handleOverlapping(abstractWorld)
  unifyWorld(abstractWorld)
  closeDeadEnds(abstractWorld)
}

//data class WorldBundle(val abstractWorld: AbstractWorld, val structureWorld: StructureWorld)

fun generateWorld(abstractWorld: AbstractWorld, structureWorld: StructureWorld, dice: Dice) {
  generateAbstract(abstractWorld, dice)
  generateStructure(abstractWorld, structureWorld)
//  return WorldBundle(abstractWorld, structureWorld)
}