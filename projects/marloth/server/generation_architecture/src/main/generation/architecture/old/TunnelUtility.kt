package generation.architecture.old

import generation.architecture.misc.getNodeDistance
import mythic.ent.Id
import mythic.spatial.Vector3
import simulation.misc.Graph
import simulation.misc.nodeNeighbors2

data class TunnelInfo(
    val start: Vector3,
    val vector: Vector3,
    val length: Float
)

fun getTunnelInfo(graph: Graph, node: Id): TunnelInfo {
  val neighbors = nodeNeighbors2(graph.connections.asSequence(), node).map { graph.nodes[it]!! }
  val overlap = 1f
  val length = getNodeDistance(neighbors[0], neighbors[1]) + overlap
  val horizontalVector = (neighbors[0].position.copy(z = 0f) - neighbors[1].position.copy(z = 0f)).normalize()
  val start = neighbors[1].position + horizontalVector * neighbors[1].radius
  val end = neighbors[0].position - horizontalVector * neighbors[0].radius
  val vector = (end - start).normalize()

  return TunnelInfo(
      start = start,
      vector = vector,
      length = length
  )
}
