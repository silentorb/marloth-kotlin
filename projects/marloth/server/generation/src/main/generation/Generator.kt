package generation

import generation.abstract.*
import generation.structure.*
import mythic.ent.newIdSource
import mythic.spatial.*
import randomly.Dice
import simulation.*

fun getTwinTunnels(graph: Graph, tunnels: List<PreTunnel>): List<PreTunnel> =
    crossMap(tunnels.asSequence()) { a: PreTunnel, b: PreTunnel ->
      //      println("" + a.neighbors.any { b.neighbors.contains(it) } + ", " + a.position.distance(b.position))
      val c = a.connection.nodes(graph).any { b.connection.nodes(graph).contains(it) }
          && a.position.distance(b.position) < doorwayLength * 2f
//      println(c)
      c
    }

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y) / (100 * 100)

fun generateWorld(input: WorldInput): World {
  val scale = calculateWorldScale(input.boundary.dimensions)
  val biomeGrid = newBiomeGrid(input)
  val (graph, tunnels) = generateAbstract(input, scale, biomeGrid)
  val idSources = StructureIdSources(
      node = idSourceFromNodes(graph.nodes.values),
      face = newIdSource(1),
      edge = newIdSource(1)
  )
  val realm1 = generateStructure(idSources, graph, input.dice, tunnels)
  val realm2 = realm1.copy(
      nodes = realm1.nodes.mapValues { (_, node) ->
        if (node.biome == Biome.void)
          node.copy(biome = biomeGrid(node.position.x, node.position.y))
        else
          node
      }
  )

  val texturedFaces = assignTextures(realm2.nodes, realm2.connections)

  val finalRealm = simulation.Realm(
      boundary = input.boundary,
      nodeList = realm2.nodes.values.toList(),
      faces = texturedFaces,
      mesh = realm2.mesh
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
  val newCharacters = placeEnemies(world.realm, nextId, dice, scale)
  return addDeck(world, newCharacters, nextId)
}