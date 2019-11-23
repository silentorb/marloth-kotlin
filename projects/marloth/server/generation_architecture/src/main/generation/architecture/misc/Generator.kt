package generation.architecture.misc

import generation.abstracted.applyInitialBiomes
import generation.abstracted.gridToGraph
import generation.general.BiomeGrid
import mythic.ent.pipe2
import mythic.spatial.Vector3
import simulation.misc.Graph
import simulation.misc.MapGrid
import simulation.misc.Realm
import simulation.misc.WorldInput

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y * dimensions.z) / (100 * 100 * 100)


fun generateAbstract(config: GenerationConfig, input: WorldInput, biomeGrid: BiomeGrid): (Graph) -> Graph =
    pipe2(listOf(
        { graph -> graph.copy(nodes = applyInitialBiomes(config.biomes, biomeGrid, graph)) }
    ))

fun generateRealm(config: GenerationConfig, input: WorldInput, grid: MapGrid, biomeGrid: BiomeGrid): Realm {
  val scale = calculateWorldScale(input.boundary.dimensions)
  val (initialGraph, cellMap) = gridToGraph()(grid)
  val graph = generateAbstract(config, input, biomeGrid)(initialGraph)

  return Realm(
      graph = graph,
      cellMap = cellMap,
      cellBiomes = applyBiomesToGrid(grid, biomeGrid),
      nodeList = graph.nodes.values.toList(),
      doorFrameNodes = graph.doorways,
      grid = grid
  )
}
