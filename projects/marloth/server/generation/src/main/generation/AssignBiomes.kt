package generation

import mythic.ent.Id
import simulation.*

typealias BiomeMap = Map<Id, Biome>

fun newBiomeGrid(input: WorldInput, biomes: List<Biome>): Grid<Biome> {
  val gridScale = 0.5f
  val dimensions = input.boundary.dimensions * gridScale
  val width = dimensions.x.toInt()
  val height = dimensions.y.toInt()
  val anchorCount = (width * height * 0.1f).toInt()
  val anchors = voronoiAnchors(biomes, anchorCount, input.dice)
  return voronoi(width, height, anchors)
}

private fun normalizeGrid(grid: Grid<Biome>, boundary: WorldBoundary): Grid<Biome> {
  val offset = (boundary.dimensions / 2f)
  return { x, y ->
    grid(
        (x + offset.x) / boundary.dimensions.x,
        (y + offset.y) / boundary.dimensions.y
    )
  }
}

private fun logGrid(grid: Grid<Biome>, boundary: WorldBoundary) {
  for (y in 0 until (boundary.dimensions.y).toInt()) {
    val line = (0 until (boundary.dimensions.x).toInt()).map { x ->
      grid(
          x.toFloat() - boundary.dimensions.x / 2f,
          y.toFloat() - boundary.dimensions.y / 2f)
          .ordinal
    }
        .joinToString(" ")
    println(line)
  }
}

typealias BiomeGrid = Grid<Biome>

fun newBiomeGrid(input: WorldInput) =
    normalizeGrid(clampGrid(newBiomeGrid(input, randomBiomes)), input.boundary)

fun assignBiomes(nodes: Collection<Node>, grid: BiomeGrid, home: List<Node>): BiomeMap {
  return nodes.associate { node ->
    val biome = if (home.any { it.id == node.id })
      Biome.home
    else
      grid(node.position.x, node.position.y)

    Pair(node.id, biome)
  }
}

fun fillNodeBiomes(biomeGrid: BiomeGrid, nodes: NodeTable) =
    nodes.mapValues { (_, node) ->
      if (node.biome == Biome.void)
        node.copy(biome = biomeGrid(node.position.x, node.position.y))
      else
        node
    }
