package generation

import generation.abstracted.*
import generation.structure.*
import mythic.ent.newIdSource
import mythic.spatial.*
import randomly.Dice
import simulation.*

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y) / (100 * 100)

fun generateWorld(input: WorldInput): World {
  val scale = calculateWorldScale(input.boundary.dimensions)
  val biomeGrid = newBiomeGrid(input)
  val graph = generateAbstract(input, scale, biomeGrid)
  val idSources = StructureIdSources(
      node = idSourceFromNodes(graph.nodes.values),
      face = newIdSource(1),
      edge = newIdSource(1)
  )
  val realm1 = generateStructure(biomeGrid, idSources, graph, input.dice)
  val realm2 = realm1
//      .copy(
//      nodes = fillNodeBiomes(biomeGrid, realm1.nodes)
//  )

  val texturedFaces = assignTextures(realm2.nodes, realm2.connections)

  val finalRealm = simulation.Realm(
      boundary = input.boundary,
      nodeList = realm2.nodes.values.toList(),
      faces = texturedFaces,
      mesh = realm2.mesh,
      doorFrameNodes = graph.doorways
  )

  return finalizeRealm(input, finalRealm)
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
  val nextId = newIdSource(world.nextId)
  val newCharacters = placeCharacters(world.realm, nextId, dice, scale)
  return addDeck(world, newCharacters, nextId)
}