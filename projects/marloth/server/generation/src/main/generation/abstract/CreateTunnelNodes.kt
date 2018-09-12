package generation.abstract

import generation.getCenter
import mythic.spatial.Vector3m

const val tunnelRadius = 1f

data class PreTunnel(
    val connection: Connection,
    val position: Vector3m
)

fun prepareTunnels(graph: NodeGraph): List<PreTunnel> =
    graph.connections.filter { it.type == ConnectionType.tunnel }
        .map { oldConnection ->
          PreTunnel(
              connection = oldConnection,
              position = getCenter(oldConnection.first, oldConnection.second)
              )
        }

fun createTunnelNodes(realm: Realm, preTunnels: List<PreTunnel>): List<Node> {
  val graph = realm.graph
  return preTunnels
      .map { preTunnel ->
        val oldConnection = preTunnel.connection
        val tunnelNode = Node(
            id = realm.nextId(),
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