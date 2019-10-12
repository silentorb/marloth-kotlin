package generation.abstracted

import generation.misc.getCenter
import generation.misc.idSourceFromNodes
import mythic.ent.entityMap
import mythic.spatial.Vector3
import simulation.misc.*

const val tunnelRadius = 1f

data class PreTunnel(
    val connection: InitialConnection,
    val position: Vector3
)

fun prepareTunnels(graph: Graph): List<PreTunnel> =
    graph.connections.filter { it.type == ConnectionType.doorway }
        .map { oldConnection ->
          PreTunnel(
              connection = oldConnection,
              position = getCenter(graph.node(oldConnection.first)!!, graph.node(oldConnection.second)!!)
          )
        }

fun createTunnelNodes(graph: Graph, preTunnels: List<PreTunnel>): Graph {
  val nextId = idSourceFromNodes(graph.nodes.values)
  val nodes = preTunnels
      .map { preTunnel ->
        Node(
            id = nextId(),
//            position = getCenter(graph.node(oldConnection.first)!!, graph.node(oldConnection.second)!!),
            position = preTunnel.position,
            radius = tunnelRadius
        )
      }

  val connections = nodes
      .zip(preTunnels) { node, preTunnel ->
        val oldConnection = preTunnel.connection
        listOf(
            InitialConnection(oldConnection.first, node.id, ConnectionType.doorway),
            InitialConnection(oldConnection.second, node.id, ConnectionType.doorway)
        )
      }.flatten()

  return Graph(entityMap(nodes), connections)
}
