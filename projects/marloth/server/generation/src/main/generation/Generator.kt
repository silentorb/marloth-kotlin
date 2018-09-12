package generation

import generation.abstract.*
import generation.structure.doorwayLength
import generation.structure.generateStructure
import mythic.spatial.*
import randomly.Dice
import simulation.*

fun createRoomNode(realm: Realm, biomes: List<Biome>, dice: Dice): Node {
  val radius = dice.getFloat(5f, 10f)
  val start = realm.boundary.start + radius
  val end = realm.boundary.end - radius

  val node = Node(
      position = Vector3m(dice.getFloat(start.x, end.x), dice.getFloat(start.y, end.y), 0f),
      radius = radius,
      biome = dice.getItem(biomes),
      isSolid = false,
      isWalkable = true
  )
  realm.nodes.add(node)
  return node
}

fun createRoomNodes(count: Int, world: Realm, biomes: List<Biome>, dice: Dice) {
  for (i in 0..count) {
    createRoomNode(world, biomes, dice)
  }
}

fun getTwinTunnels(tunnels: List<PreTunnel>): List<PreTunnel> =
    crossMap(tunnels.asSequence()) { a: PreTunnel, b: PreTunnel ->
      //      println("" + a.neighbors.any { b.neighbors.contains(it) } + ", " + a.position.distance(b.position))
      val c = a.connection.nodes.any { b.connection.nodes.contains(it) }
          && a.position.distance(b.position) < doorwayLength * 2f
//      println(c)
      c
    }

fun generateAbstract(world: Realm, biomes: List<Biome>, dice: Dice, scale: Float): List<Node> {
  val nodeCount = (20 * scale).toInt()
  createRoomNodes(nodeCount, world, biomes, dice)
  handleOverlapping(world.graph)
  unifyWorld(world.graph)
  closeDeadEnds(world.graph)

  val preTunnels = prepareTunnels(world.graph)
  val twinTunnels = getTwinTunnels(preTunnels)
  val tunnels = createTunnelNodes(world, preTunnels.minus(twinTunnels))
  twinTunnels.forEach { world.graph.disconnect(it.connection) }

  fillIndexes(world.graph)
  return tunnels
}

fun fillIndexes(graph: NodeGraph) {
  var index = 0
  for (node in graph.nodes) {
    node.index = index++
  }
}

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y) / (100 * 100)

fun generateWorld(input: WorldInput): World {
  val biomes = createBiomes()
  val realm = Realm(input.boundary)
  val scale = calculateWorldScale(realm.boundary.dimensions)
  val tunnels = generateAbstract(realm, biomes, input.dice, scale)
  generateStructure(realm, input.dice, tunnels)
  val playerNode = realm.nodes.first()
  val nextId = newIdSource(1)
  val deck = Deck(
      factions = listOf(
          Faction(1, "Misfits"),
          Faction(2, "Monsters")
      )
  )
      .plus(toDeck(newPlayer(nextId, playerNode)))
      .plus(placeWallLamps(realm, nextId, input.dice, scale))
//  instantiator.newPlayer(1)

//  placeWallLamps(world, instantiator, input.dice, scale)

  return World(
      deck = deck,
      nextId = nextId(),
      realm = realm)
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