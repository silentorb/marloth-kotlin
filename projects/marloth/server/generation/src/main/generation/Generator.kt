package generation

import generation.abstract.*
import generation.abstract.Node
import generation.abstract.Realm
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
  val initialRealm = Realm(input.boundary)
  val scale = calculateWorldScale(initialRealm.boundary.dimensions)
  val tunnels = generateAbstract(initialRealm, biomes, input.dice, scale)
  generateStructure(initialRealm, input.dice, tunnels)
  val realm = simulation.Realm(
      boundary = initialRealm.boundary,
      nodes = initialRealm.nodes.map {
        simulation.Node(
            id = it.index.toLong(),
            position = Vector3(it.position),
            height = it.height,
            isWalkable = it.isWalkable,
            biome = it.biome,
            isSolid = it.isSolid,
            floors = it.floors.toList(),
            ceilings = it.ceilings.toList(),
            walls = it.walls.toList()
        )
      },
      mesh = initialRealm.mesh
  )
  val getNode = { id: Long? -> realm.nodes.firstOrNull { it.id == id } }
  realm.mesh.faces.forEach { face ->
    val data = generation.abstract.getFaceInfo(face)
    face.data = simulation.FaceInfo(
        type = data.type,
        firstNode = getNode(data.firstNode?.index?.toLong()),
        secondNode = getNode(data.secondNode?.index?.toLong()),
        texture = data.texture,
        debugInfo = data.debugInfo
    )
  }
  return finalizeRealm(input, realm)
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