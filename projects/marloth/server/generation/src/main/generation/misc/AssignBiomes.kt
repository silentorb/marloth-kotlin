package generation.misc

import simulation.misc.BiomeName
import simulation.misc.WorldInput

typealias BiomeGrid = Grid<BiomeName>

fun newBiomeGrid(biomes: BiomeInfoMap, input: WorldInput): BiomeGrid {
  val gridScale = 0.5f
  val dimensions = input.boundary.dimensions * gridScale
  val width = dimensions.x.toInt()
  val height = dimensions.y.toInt()
  val anchorCount = (width * height * 0.1f).toInt()
  val boundary = input.boundary
  val values = biomes.keys.toList()
  val anchors = voronoiAnchors(values, anchorCount, input.dice, boundary.start, boundary.end)
  return voronoi(anchors)
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

val fixedDistributionBiomeAttributes = setOf(
    BiomeAttribute.placeOnlyAtStart,
    BiomeAttribute.placeOnlyAtEnd
)

fun randomDistributionBiomes(biomes: BiomeInfoMap): BiomeInfoMap =
    biomes.filterValues { biome -> biome.attributes.none { fixedDistributionBiomeAttributes.contains(it) } }

fun newRandomizedBiomeGrid(biomes: BiomeInfoMap, input: WorldInput) =
    newBiomeGrid(randomDistributionBiomes(biomes), input)
