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

fun createTunnelNodes(graph: Graph, preTunnels: List<PreTunnel>): Graph {
  val nextId = newIdSource(graph.nodes.size + 1L)
  val nodes = preTunnels
      .map { preTunnel ->
        val oldConnection = preTunnel.connection
        val tunnelNode = Node(
            id = nextId(),
            position = getCenter(graph.node(oldConnection.first)!!, graph.node(oldConnection.second)!!),
            radius = tunnelRadius,
            isSolid = false,
            isWalkable = true)

//        graph.disconnect(preTunnel.connection)
//        graph.connect(oldConnection.first, tunnelNode, ConnectionType.tunnel)
//        graph.connect(oldConnection.second, tunnelNode, ConnectionType.tunnel)
//        graph.nodes.add(tunnelNode)
        tunnelNode
      }

  val connections = nodes
      .zip(preTunnels) { node, preTunnel ->
        val oldConnection = preTunnel.connection
        listOf(
            Connection(oldConnection.first, node.id, ConnectionType.tunnel),
            Connection(oldConnection.second, node.id, ConnectionType.tunnel)
            )
      }.flatten()

  return Graph(nodes, connections)
}