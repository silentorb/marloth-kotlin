package generation.abstract

import generation.getCenter
import mythic.spatial.Vector3m
import simulation.newIdSource

const val tunnelRadius = 1f

data class PreTunnel(
    val connection: Connection,
    val position: Vector3m
)

fun prepareTunnels(graph: Graph): List<PreTunnel> =
    graph.connections.filter { it.type == ConnectionType.tunnel }
        .map { oldConnection ->
          PreTunnel(
              connection = oldConnection,
              position = getCenter(graph.node(oldConnection.first)!!, graph.node(oldConnection.second)!!)
              )
        }

fun createTunnelNodes(graph: Graph, preTunnels: List<PreTunnel>): Pair<Graph, List<Node>> {
  val nextId = newIdSource(graph.nodes.size + 1L)
  return preTunnels
      .map { preTunnel ->
        val oldConnection = preTunnel.connection
        val tunnelNode = Node(
            id = nextId(),
            position = getCenter(oldConnection.first, oldConnection.second),
            radius = tunnelRadius,
            isSolid = false,
            isWalkable = true)

        graph.disconnect(preTunnel.connection)
        graph.connect(oldConnection.first, tunnelNode, ConnectionType.tunnel)
        graph.connect(oldConnection.second, tunnelNode, ConnectionType.tunnel)
        graph.nodes.add(tunnelNode)
        tunnelNode
      }
}