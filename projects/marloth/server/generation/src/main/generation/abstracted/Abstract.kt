package generation.abstracted

import generation.misc.BiomeAttribute
import generation.misc.BiomeGrid
import generation.misc.BiomeInfoMap
import generation.misc.GenerationConfig
import mythic.ent.Id
import mythic.ent.newIdSource
import mythic.ent.pipe2
import simulation.misc.*

const val minInitialNodeDistance = 1.5f
const val minInitialNodeSize = 5f
const val maxInitialNodeSize = 10f

fun connections(graph: Graph, node: Node): List<InitialConnection> =
    graph.connections.filter { it.contains(node) }

fun neighbors(graph: Graph, node: Node): Sequence<Node> = connections(graph, node).asSequence().mapNotNull { graph.node(it.other(node)) }

fun connections(graph: Graph, node: Id): List<InitialConnection> =
    graph.connections.filter { it.contains(node) }

fun neighbors(graph: Graph, node: Id): Sequence<Node> = connections(graph, node).asSequence().mapNotNull { graph.node(it.other(node)) }

fun getConnection(graph: Graph, node: Node, other: Node) = graph.connections.firstOrNull { it.contains(node) && it.contains(other) }

fun isConnected(graph: Graph, node: Node, other: Node) = getConnection(graph, node, other) != null

fun getOtherNode(graph: Graph, first: Id, pivot: Id): Node {
  val options = neighbors(graph, pivot).filter { it.id != first }.toList()
  assert(options.size == 1)
  return options.first()
}

fun applyInitialBiomes(biomes: BiomeInfoMap, biomeGrid: BiomeGrid, graph: Graph): NodeTable {
//  val deadEnds = getDeadEnds(graph)
//  val home = deadEnds[0]
//  val exit = deadEnds
//      .drop(1)
//      .filter { abs(it.position.z - home.position.z) > 5f}
//      .firstSortedByDescending { it.position.distance(home.position) }

  val homeNode = graph.nodes.values.firstOrNull { it.attributes.contains(NodeAttribute.home) }?.id
  val exitNode = graph.nodes.values.firstOrNull { it.attributes.contains(NodeAttribute.exit) }?.id
  val remainingNodes = graph.nodes
      .minus(listOfNotNull(homeNode, exitNode))

  val homeBiome = biomes.entries.firstOrNull {
    it.value.attributes.contains(BiomeAttribute.placeOnlyAtStart)
  }?.key

  val exitBiome = biomes.entries.firstOrNull {
    it.value.attributes.contains(BiomeAttribute.placeOnlyAtEnd)
  }?.key

  val fixedBiomeMap: Map<Id, BiomeName> = listOfNotNull(
      if (homeNode != null && homeBiome != null) Pair(homeNode, homeBiome) else null,
      if (exitNode != null && exitBiome != null) Pair(exitNode, exitBiome) else null
  ).associate { it }

  val biomeMap: Map<Id, BiomeName> = remainingNodes
      .mapValues { (_, node) -> biomeGrid(node.position) }
      .plus(fixedBiomeMap)

  return graph.nodes.mapValues {
    it.value.copy(
        biome = biomeMap[it.value.id]!!
    )
  }
}

//fun prepareDoorways(graph: Graph): Graph {
//  val homeNodes = graph.nodes.values.filter { it.biome == BiomeId.home }
//  val doorways = homeNodes.flatMap { node ->
//    connections(graph, node).mapNotNull { connection ->
//      val otherNode = connection.other(graph, node)
//      if (otherNode.biome != BiomeId.home && connection.type == ConnectionType.tunnel) {
//        val origin = getCenter(node, otherNode)
//        val position = origin + (otherNode.position - node.position).normalize() * 0.2f
//        PreTunnel(connection, position)
//      } else
//        null
//    }
//  }
//  val newTunnels = createTunnelNodes(graph, doorways)
//  val doorwayNodeIds = newTunnels.nodes.map { it.key }
//  return graph.copy(
//      nodes = graph.nodes.plus(newTunnels.nodes.mapValues { it.value.copy(biome = BiomeId.home) }),
//      connections = graph.connections.plus(newTunnels.connections).minus(doorways.map { it.connection }),
//      doorways = graph.doorways.plus(doorwayNodeIds)
//  )
//}

fun <A, B> pass(action: (A) -> A): (Pair<A, B>) -> Pair<A, B> = { (a, b) ->
  Pair(action(a), b)
}

fun generateAbstract(config: GenerationConfig, input: WorldInput, biomeGrid: BiomeGrid): (Graph) -> Graph =
    pipe2(listOf(
        { graph -> graph.copy(nodes = applyInitialBiomes(config.biomes, biomeGrid, graph)) }
    ))
