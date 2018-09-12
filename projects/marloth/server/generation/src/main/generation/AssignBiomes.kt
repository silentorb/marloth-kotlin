package generation

import generation.abstract.Realm
import mythic.spatial.minMax
import simulation.Biome
import simulation.Id
import simulation.WorldBoundary
import simulation.WorldInput

typealias BiomeMap = Map<Id, Biome>

private val gridScale = 0.5f

fun newBiomeGrid(input: WorldInput, biomes: List<Biome>): Grid<Biome> {
  val dimensions = input.boundary.dimensions * gridScale
  val width = dimensions.x.toInt()
  val height = dimensions.y.toInt()
  val anchorCount = (width * height * 0.2f).toInt()
  val anchors = voronoiAnchors(biomes, anchorCount, input.dice)
  return voronoi(width, height, anchors)
}

private fun normalizeGrid(grid: Grid<Biome>, boundary: WorldBoundary): Grid<Biome> {
  val offset = (boundary.dimensions / 2f)
  return { x, y ->
    grid(
        (x + offset.x) * gridScale / boundary.dimensions.x,
        (y + offset.y) * gridScale / boundary.dimensions.y
    )
  }
}

fun assignBiomes(realm: Realm, biomes: List<Biome>, input: WorldInput): BiomeMap {
  val grid = normalizeGrid(clampGrid(newBiomeGrid(input, biomes)), input.boundary)
  val boundary = input.boundary
  for (y in 0 until (boundary.dimensions.y * gridScale).toInt()) {
    val line = (0 until (boundary.dimensions.x * gridScale).toInt()).map { x -> biomes.indexOf(grid(x.toFloat(), y.toFloat())) }
        .joinToString(" ")
    println(line)
  }
  return realm.nodes.associate { node ->
    val biome = grid(
        node.position.x,
        node.position.y
    )
    Pair(node.id, biome)
  }
}