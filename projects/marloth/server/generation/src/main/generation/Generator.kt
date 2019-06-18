package generation

import generation.abstracted.Graph
import generation.abstracted.generateAbstract
import generation.structure.StructureIdSources
import generation.structure.assignTextures
import generation.structure.generateStructure
import generation.structure.idSourceFromNodes
import mythic.ent.newIdSource
import mythic.ent.pipe
import mythic.spatial.Vector3
import randomly.Dice
import simulation.*

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y * dimensions.z) / (100 * 100 * 100)

fun generateWorld(input: WorldInput): Pair<World, Graph> {
  val scale = calculateWorldScale(input.boundary.dimensions)
  val biomeGrid = newBiomeGrid(input)
  val graph = generateAbstract(input, scale, biomeGrid)
  val idSources = StructureIdSources(
      node = idSourceFromNodes(graph.nodes.values),
      face = newIdSource(1),
      edge = newIdSource(1)
  )
//  val realm1 = generateStructure(biomeGrid, idSources, graph, input.dice)
//  val realm2 = realm1
//      .copy(
//      nodes = fillNodeBiomes(biomeGrid, realm1.nodes)
//  )

//  val texturedFaces = assignTextures(graph.nodes, realm2.connections)

  val finalRealm = simulation.Realm(
      boundary = input.boundary,
      nodeList = graph.nodes.values.toList(),
      faces = mapOf(),
//      mesh = realm2.mesh,
      doorFrameNodes = graph.doorways
  )

  return Pair(finalizeRealm(input, finalRealm), graph)
}

fun generateDefaultWorld(): World {
  val input = WorldInput(
      boundary = createWorldBoundary(50f),
      dice = Dice(2)
  )
  val (world, _) = generateWorld(input)
  return addEnemies(world, input.boundary, input.dice)
}

fun addEnemies(world: World, boundary: WorldBoundary, dice: Dice): World {
  val scale = calculateWorldScale(boundary.dimensions)
  return pipe(world, listOf(
      addDeck(placeCharacters(world.realm, dice, scale))
  ))
}
