package generation.abstract

import generation.getCenter
import simulation.AbstractWorld
import simulation.ConnectionType
import simulation.Node

fun createTunnelNodes(world: AbstractWorld): List<Node> {
  val graph = world.graph
  return graph.connections.filter { it.type == ConnectionType.tunnel }
      .map { oldConnection ->
        val tunnelNode = Node(
            position = getCenter(oldConnection.first, oldConnection.second),
            radius = 1f,
            isSolid = false,
            isWalkable = true)

        graph.disconnect(oldConnection)
        graph.connect(oldConnection.first, tunnelNode, ConnectionType.tunnel)
        graph.connect(oldConnection.second, tunnelNode, ConnectionType.tunnel)
        graph.nodes.add(tunnelNode)
        tunnelNode
      }
}