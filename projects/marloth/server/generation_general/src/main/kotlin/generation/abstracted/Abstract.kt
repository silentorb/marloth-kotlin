package generation.abstracted

import generation.general.BiomeAttribute
import generation.general.BiomeGrid
import generation.general.BiomeInfoMap
import silentorb.mythic.ent.Id
import simulation.misc.*

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

//fun applyInitialBiomes(biomes: BiomeInfoMap, biomeGrid: BiomeGrid, grid: MapGrid): CellTable {
//  val homeNode = grid.cells.entries.firstOrNull { it.value.attributes.contains(NodeAttribute.home) }?.key
//  val exitNode = grid.cells.entries.firstOrNull { it.value.attributes.contains(NodeAttribute.exit) }?.key
//  val remainingNodes = grid.cells.keys
//      .minus(listOfNotNull(homeNode, exitNode))
//
//  val homeBiome = biomes.entries.firstOrNull {
//    it.value.attributes.contains(BiomeAttribute.placeOnlyAtStart)
//  }?.key
//
//  val exitBiome = biomes.entries.firstOrNull {
//    it.value.attributes.contains(BiomeAttribute.placeOnlyAtEnd)
//  }?.key
//
//  val fixedBiomeMap: Map<Id, BiomeName> = listOfNotNull(
//      if (homeNode != null && homeBiome != null) Pair(homeNode, homeBiome) else null,
//      if (exitNode != null && exitBiome != null) Pair(exitNode, exitBiome) else null
//  ).associate { it }
//
//  val biomeMap: Map<Id, BiomeName> = remainingNodes
//      .mapValues { (_, node) -> biomeGrid(node.position) }
//      .plus(fixedBiomeMap)
//
//  return grid.cells.keys.associateWith {
//    it.value.copy(
//        biome = biomeMap[it.value.id]!!
//    )
//  }
//}

fun <A, B> pass(action: (A) -> A): (Pair<A, B>) -> Pair<A, B> = { (a, b) ->
  Pair(action(a), b)
}
