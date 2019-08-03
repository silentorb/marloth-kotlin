package generation

import simulation.misc.Graph
import generation.abstracted.generateAbstract
import generation.misc.newBiomeGrid
import mythic.ent.pipe
import mythic.spatial.Vector3
import randomly.Dice
import simulation.main.World
import simulation.main.addDeck
import simulation.misc.Realm
import simulation.misc.WorldBoundary
import simulation.misc.WorldInput
import simulation.misc.createWorldBoundary

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y * dimensions.z) / (100 * 100 * 100)

fun generateWorld(input: WorldInput): World {
  val scale = calculateWorldScale(input.boundary.dimensions)
  val biomeGrid = newBiomeGrid(input)
  val (grid, graph, cellMap) = generateAbstract(input, scale, biomeGrid)

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

fun generateDefaultWorld(): World {
  val input = WorldInput(
      boundary = createWorldBoundary(50f),
      dice = Dice(2)
  )
  val world = generateWorld(input)
  return addEnemies(world, input.boundary, input.dice)
}

fun addEnemies(world: World, boundary: WorldBoundary, dice: Dice): World {
  val scale = calculateWorldScale(boundary.dimensions)
  return pipe(world, listOf(
      addDeck(placeCharacters(world.realm, dice, scale))
  ))
}
