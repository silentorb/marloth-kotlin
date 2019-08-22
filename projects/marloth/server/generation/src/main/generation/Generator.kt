package generation

import generation.abstracted.generateAbstract
import generation.misc.GenerationConfig
import generation.misc.newNormalizedBiomeGrid
import mythic.ent.newIdSource
import mythic.spatial.Vector3
import randomly.Dice
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Realm
import simulation.misc.WorldInput

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y * dimensions.z) / (100 * 100 * 100)

fun finalizeRealm(realm: Realm): World {
  val nextId = newIdSource(1)
  return World(
      deck = Deck(),
      nextId = nextId(),
      realm = realm,
      dice = Dice(),
      availableIds = setOf(),
      logicUpdateCounter = 0
  )
}

fun generateWorld(config: GenerationConfig, input: WorldInput): World {
  val scale = calculateWorldScale(input.boundary.dimensions)
  val biomeGrid = newNormalizedBiomeGrid(config.biomes, input)
  val (grid, graph, cellMap) = generateAbstract(config, input, scale, biomeGrid)

  val finalRealm = Realm(
      graph = graph,
      cellMap = cellMap,
      nodeList = graph.nodes.values.toList(),
      faces = mapOf(),
//      mesh = realm2.mesh,
      doorFrameNodes = graph.doorways,
      grid = grid
  )

  return finalizeRealm(finalRealm)
}
