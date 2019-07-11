package generation.abstracted

import generation.misc.getNodeDistance
import mythic.ent.firstSortedBy
import randomly.Dice

fun variableHeights(dice: Dice): (Graph) -> Graph = { graph ->
  var currentGraph = graph
  val keys = currentGraph.nodes.keys.toList()
  for (i in 0 until graph.nodes.size) {
    val id = keys[i]
    val node = currentGraph.nodes[id]!!
    val neighbors = nodeNeighbors(graph.connections, id)
    val gaps = neighbors.map { otherId ->
      val other = graph.nodes[otherId]!!
      getNodeDistance(node, other)
    }
    val smallestGap = gaps.firstSortedBy { it }
    val heightRange = smallestGap * 0.7f // Angles would be more precise but with a buffer, linear works fine.
    val offset = dice.getFloat(-heightRange, heightRange)
    val newNode = node.copy(
        position = node.position.copy(
            z = node.position.z + offset
        )
    )
    currentGraph = currentGraph.copy(
        nodes = currentGraph.nodes.plus(Pair(id, newNode))
    )
  }
  currentGraph
}
