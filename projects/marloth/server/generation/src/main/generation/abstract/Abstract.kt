package generation.abstract

import gatherNodes
import generation.BiomeGrid
import generation.assignBiomes
import generation.getTwinTunnels
import mythic.ent.Id
import mythic.spatial.Vector3
import randomly.Dice
import simulation.*

fun createRoomNode(boundary: WorldBoundary, id: Id, dice: Dice): Node {
  val radius = dice.getFloat(5f, 10f)
  val start = boundary.start + radius
  val end = boundary.end - radius

  return Node(
      id = id,
      position = Vector3(dice.getFloat(start.x, end.x), dice.getFloat(start.y, end.y), 0f),
      radius = radius,
      isSolid = false,
      isWalkable = true,
      biome = Biome.void,
      height = 0f,
      floors = mutableListOf(),
      ceilings = mutableListOf(),
      walls = mutableListOf()
  )
}

fun createRoomNodes(boundary: WorldBoundary, count: Int, dice: Dice) =
    (1L..count).map { id ->
      createRoomNode(boundary, id, dice)
    }

fun getHome(graph: Graph): List<Node> {
  val start = getDeadEnds(graph).first()
  return gatherNodes(listOf(start)) { node ->
    node.neighbors(graph).filter { it.isWalkable && it.getConnection(graph, node)!!.type == ConnectionType.union }.toList()
  }
}

fun generateAbstract(input: WorldInput, scale: Float, biomeGrid: BiomeGrid): Pair<Graph, List<Node>> {
  val nodeCount = (20 * scale).toInt()
  val initialNodes = createRoomNodes(input.boundary, nodeCount, input.dice)
  val graph = handleOverlapping(initialNodes)
  val unifyingConnections = unifyWorld(graph)
  val secondConnections = graph.connections.plus(unifyingConnections)
  val deadEndClosingConnections = closeDeadEnds(graph.copy(connections = secondConnections))
  val thirdGraph = graph.copy(connections = secondConnections.plus(deadEndClosingConnections))

  val preTunnels = prepareTunnels(thirdGraph)
  val twinTunnels = getTwinTunnels(thirdGraph, preTunnels)
  val tunnelGraph = createTunnelNodes(thirdGraph, preTunnels.minus(twinTunnels))

  val fourthGraph = thirdGraph.plus(tunnelGraph).minusConnections(preTunnels.plus(twinTunnels).map { it.connection })
  val home = getHome(graph)
  val biomeMap = assignBiomes(fourthGraph.nodes, biomeGrid, home)
  val fifthGraph = fourthGraph.copy(
      nodes = fourthGraph.nodes.map {
        it.copy(
            biome = biomeMap[it.id]!!
        )
      }
  )

  return Pair(fifthGraph, tunnelGraph.nodes)
}
