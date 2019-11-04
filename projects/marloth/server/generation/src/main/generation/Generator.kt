package generation

import generation.abstracted.generateAbstract
import generation.abstracted.gridToGraph
import generation.elements.applyBiomesToGrid
import generation.misc.BiomeGrid
import generation.misc.GenerationConfig
import mythic.spatial.Vector3
import simulation.misc.MapGrid
import simulation.misc.Realm
import simulation.misc.WorldInput

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y * dimensions.z) / (100 * 100 * 100)

//fun finalizeRealm(realm: Realm): World {
//  val nextId = newIdSource(1)
//  return World(
//      deck = Deck(),
//      nextId = nextId(),
//      realm = realm,
//      dice = Dice(),
//      availableIds = setOf(),
//      logicUpdateCounter = 0
//  )
//}

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
