package generation.abstract

import generation.getCenter
import simulation.AbstractWorld
import simulation.ConnectionType
import simulation.Node
import simulation.NodeType

fun createTunnelNodes(world: AbstractWorld) {
  val graph = world.graph
  graph.connections.filter { it.type == ConnectionType.tunnel }
      .forEach { oldConnection ->
        val tunnelNode = Node(getCenter(oldConnection.first, oldConnection.second), 1f, NodeType.tunnel)

        graph.disconnect(oldConnection)
        graph.connect(oldConnection.first, tunnelNode, ConnectionType.tunnel)
        graph.connect(oldConnection.second, tunnelNode, ConnectionType.tunnel)
        graph.nodes.add(tunnelNode)
      }
}