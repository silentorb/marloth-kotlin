package generation.abstracted

import gatherNodes
import generation.BiomeGrid
import generation.assignBiomes
import generation.crossMap
import generation.getCenter
import generation.structure.doorwayLength
import generation.structure.getDoorFramePoints
import mythic.ent.Id
import mythic.ent.entityMap
import mythic.ent.pipe
import mythic.spatial.Vector3
import mythic.spatial.lineSegmentIntersectsLineSegment
import randomly.Dice
import simulation.*

fun connections(graph: Graph, node: Node): List<InitialConnection> =
    graph.connections.filter { it.contains(node) }

fun neighbors(graph: Graph, node: Node): Sequence<Node> = connections(graph, node).asSequence().mapNotNull { graph.node(it.getOther(node)) }

fun connections(graph: Graph, node: Id): List<InitialConnection> =
    graph.connections.filter { it.contains(node) }

fun neighbors(graph: Graph, node: Id): Sequence<Node> = connections(graph, node).asSequence().mapNotNull { graph.node(it.getOther(node)) }

fun getConnection(graph: Graph, node: Node, other: Node) = graph.connections.firstOrNull { it.contains(node) && it.contains(other) }

fun isConnected(graph: Graph, node: Node, other: Node) = getConnection(graph, node, other) != null

fun getOtherNode(graph: Graph, first: Id, pivot: Id): Node {
  val options = neighbors(graph, pivot).filter { it.id != first }.toList()
  assert(options.size == 1)
  return options.first()
}

fun faceNodes(info: ConnectionFace) =
    listOf(info.firstNode, info.secondNode)

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
      height = 0f
  )
}

fun createRoomNodes(boundary: WorldBoundary, count: Int, dice: Dice) =
    (1L..count).map { id ->
      createRoomNode(boundary, id, dice)
    }

fun getHome(graph: Graph): List<Node> {
  val start = getDeadEnds(graph).first()
  return gatherNodes(listOf(start)) { node ->
    neighbors(graph, node).filter { it.isWalkable && getConnection(graph, it, node)!!.type == ConnectionType.union }.toList()
  }
}

fun getTwinTunnels(graph: Graph, tunnels: List<PreTunnel>): List<PreTunnel> =
//    crossMap(tunnels.asSequence()) { a: PreTunnel, b: PreTunnel ->
//      //      println("" + a.neighbors.any { b.neighbors.contains(it) } + ", " + a.position.distance(b.position))
//      if (a.connection.contains(15) || b.connection.contains(15)) {
//        val k = 0
//      }
//      val c = a.connection.nodes(graph).any { b.connection.nodes(graph).contains(it) }
//          && a.position.distance(b.position) < doorwayLength * 2f
////      println(c)
//      c
//    }
    graph.nodes.values.flatMap { node ->
      val nodeTunnels = tunnels.filter { tunnel -> tunnel.connection.contains(node.id) }
      if (node.id == 15L) {
        val k = 0
      }
      crossMap(nodeTunnels) { a, b ->
        val first = getDoorFramePoints(node, a.connection.getOther(graph, node))
        val second = getDoorFramePoints(node, b.connection.getOther(graph, node))
        val (c, _) = lineSegmentIntersectsLineSegment(first[0], first[1], second[0], second[1])
//        val c = a.position.distance(b.position) < doorwayLength * 2f
//      println(c)
        c
      }
//      listOf<PreTunnel>()
    }

fun cleanupWorld(graph: Graph): Graph {
  val unifyingConnections = unifyWorld(graph)
  val secondConnections = graph.connections.plus(unifyingConnections)
  val deadEndClosingConnections = closeDeadEnds(graph.copy(connections = secondConnections))
  return graph.copy(connections = secondConnections.plus(deadEndClosingConnections))
}

fun applyInitialBiomes(biomeGrid: BiomeGrid, graph: Graph): Graph {
  val home = getHome(graph)
  val biomeMap = assignBiomes(graph.nodes.values, biomeGrid, home)
  return graph.copy(
      nodes = graph.nodes.mapValues {
        it.value.copy(
            biome = biomeMap[it.value.id]!!
        )
      }
  )
}

fun createAndMixTunnels(graph: Graph): Graph {
  val preTunnels = prepareTunnels(graph)
  val twinTunnels = getTwinTunnels(graph, preTunnels)
  val tunnelGraph = createTunnelNodes(graph, preTunnels.minus(twinTunnels))
  return graph.plus(tunnelGraph).minusConnections(preTunnels.plus(twinTunnels).map { it.connection })
      .copy(tunnels = tunnelGraph.nodes.map { it.key })
}

fun prepareDoorways(graph: Graph): Graph {
  val homeNodes = graph.nodes.values.filter { it.biome == Biome.home }
  val doorways = homeNodes.flatMap { node ->
    connections(graph, node).mapNotNull { connection ->
      val otherNode = connection.getOther(graph, node)
      if (otherNode.biome != Biome.home && connection.type == ConnectionType.tunnel) {
        val origin = getCenter(node, otherNode)
        val position = origin + (otherNode.position - node.position).normalize() * 0.2f
        PreTunnel(connection, position)
      } else
        null
    }
  }
  val newTunnels = createTunnelNodes(graph, doorways)
  val doorwayNodeIds = newTunnels.nodes.map { it.key }
  return graph.copy(
      nodes = graph.nodes.plus(newTunnels.nodes.mapValues { it.value.copy(biome = Biome.home) }),
      connections = graph.connections.plus(newTunnels.connections).minus(doorways.map { it.connection }),
      doorways = graph.doorways.plus(doorwayNodeIds)
  )
}

fun <A, B> pass(action: (A) -> A): (Pair<A, B>) -> Pair<A, B> = { (a, b) ->
  Pair(action(a), b)
}

fun generateAbstract(input: WorldInput, scale: Float, biomeGrid: BiomeGrid): Graph {
  val nodeCount = (20 * scale).toInt()
  val initialNodes = createRoomNodes(input.boundary, nodeCount, input.dice)
  val initialGraph = handleOverlapping(entityMap(initialNodes))
  return pipe(initialGraph, listOf(
      { graph -> cleanupWorld(graph) },
      { graph -> createAndMixTunnels(graph) },
      { graph -> applyInitialBiomes(biomeGrid, graph) },
      { graph -> prepareDoorways(graph) }
  ))
}
