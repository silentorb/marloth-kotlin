package generation.misc

import mythic.ent.Id
import simulation.misc.BiomeName
import simulation.misc.WorldBoundary
import simulation.misc.WorldInput

typealias BiomeMap = Map<Id, BiomeName>

//val randomBiomes = listOf(
//    BiomeId.checkers,
//    BiomeId.forest
//)

fun newBiomeGrid(biomes: BiomeInfoMap, input: WorldInput): Grid<BiomeName> {
  val gridScale = 0.5f
  val dimensions = input.boundary.dimensions * gridScale
  val width = dimensions.x.toInt()
  val height = dimensions.y.toInt()
  val anchorCount = (width * height * 0.1f).toInt()
  val anchors = voronoiAnchors(biomes.keys.toList(), anchorCount, input.dice)
  return voronoi(width, height, anchors)
}

private fun normalizeGrid(grid: Grid<BiomeName>, boundary: WorldBoundary): Grid<BiomeName> {
  val offset = (boundary.dimensions / 2f)
  return { x, y ->
    grid(
        (x + offset.x) / boundary.dimensions.x,
        (y + offset.y) / boundary.dimensions.y
    )
  }
}

//private fun logGrid(grid: Grid<BiomeId>, boundary: WorldBoundary) {
//  for (y in 0 until (boundary.dimensions.y).toInt()) {
//    val line = (0 until (boundary.dimensions.x).toInt()).map { x ->
//      grid(
//          x.toFloat() - boundary.dimensions.x / 2f,
//          y.toFloat() - boundary.dimensions.y / 2f)
//          .ordinal
//    }
//        .joinToString(" ")
//    println(line)
//  }
//}

typealias BiomeGrid = Grid<BiomeName>

val fixedDistributionBiomeAttributes = setOf(
    BiomeAttribute.placeOnlyAtStart,
    BiomeAttribute.placeOnlyAtEnd
)

fun randomDistributionBiomes(biomes: BiomeInfoMap): BiomeInfoMap =
    biomes.filterValues { biome -> biome.attributes.none { fixedDistributionBiomeAttributes.contains(it) } }

fun newNormalizedBiomeGrid(biomes: BiomeInfoMap, input: WorldInput) =
    normalizeGrid(clampGrid(newBiomeGrid(randomDistributionBiomes(biomes), input)), input.boundary)

//fun assignBiomes(nodes: NodeTable, grid: BiomeGrid): BiomeMap {
//  return nodes.mapValues { node ->
//    val biome = grid(node.position.x, node.position.y)
//    Pair(node.id, biome)
//  }
//}

//fun fillNodeBiomes(biomeGrid: BiomeGrid, nodes: NodeTable) =
//    nodes.mapValues { (_, node) ->
//      if (node.biome == BiomeId.void)
//        node.copy(biome = biomeGrid(node.position.x, node.position.y))
//      else
//        node
//    }
