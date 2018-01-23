package generation.abstract

import generation.getCenter
import simulation.AbstractWorld
import simulation.ConnectionType
import simulation.Node
import simulation.NodeType

fun createTunnelNodes(world: AbstractWorld) {
  world.connections.filter { it.type == ConnectionType.tunnel }
      .forEach { oldConnection ->
        val tunnelNode = Node(getCenter(oldConnection.first, oldConnection.second), 1f, NodeType.tunnel)
        world.disconnect(oldConnection)
        world.connect(oldConnection.first, tunnelNode, ConnectionType.tunnel)
        world.connect(oldConnection.second, tunnelNode, ConnectionType.tunnel)
        world.nodes.add(tunnelNode)
      }
}