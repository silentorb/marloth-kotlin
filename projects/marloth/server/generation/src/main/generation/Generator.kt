package generation

import gatherNodes
import generation.abstract.*
import generation.abstract.Node
import generation.abstract.Realm
import generation.structure.assignTextures
import generation.structure.doorwayLength
import generation.structure.generateStructure
import mythic.spatial.*
import randomly.Dice
import simulation.*

fun createRoomNode(realm: Realm, dice: Dice): Node {
  val radius = dice.getFloat(5f, 10f)
  val start = realm.boundary.start + radius
  val end = realm.boundary.end - radius

  val node = Node(
      id = realm.nextId(),
      position = Vector3m(dice.getFloat(start.x, end.x), dice.getFloat(start.y, end.y), 0f),
      radius = radius,
      isSolid = false,
      isWalkable = true
  )
  realm.nodes.add(node)
  return node
}

fun createRoomNodes(count: Int, world: Realm, dice: Dice) {
  for (i in 0..count) {
    createRoomNode(world, dice)
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

fun generateAbstract(world: Realm, dice: Dice, scale: Float): List<Node> {
  val nodeCount = (20 * scale).toInt()
  createRoomNodes(nodeCount, world, dice)
  handleOverlapping(world.graph)
  unifyWorld(world.graph)
  closeDeadEnds(world.graph)

  val preTunnels = prepareTunnels(world.graph)
  val twinTunnels = getTwinTunnels(preTunnels)
  val tunnels = createTunnelNodes(world, preTunnels.minus(twinTunnels))
  twinTunnels.forEach { world.graph.disconnect(it.connection) }

//  fillIndexes(world.graph)
  return tunnels
}

//fun fillIndexes(graph: NodeGraph) {
//  var index = 1L
//  for (node in graph.nodes) {
//    node.id = index++
//  }
//}

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y) / (100 * 100)

fun getHome(graph: NodeGraph): List<Node> {
  val start = getDeadEnds(graph).first()
  return gatherNodes(listOf(start)) { node ->
    node.neighbors.filter { it.isWalkable && it.getConnection(node)!!.type == ConnectionType.union }.toList()
  }
}

fun generateWorld(input: WorldInput): World {
  val initialRealm = Realm(input.boundary)
  val scale = calculateWorldScale(initialRealm.boundary.dimensions)
  val tunnels = generateAbstract(initialRealm, input.dice, scale)
  val home = getHome(initialRealm.graph)
  generateStructure(initialRealm, input.dice, tunnels)
  val biomeMap = assignBiomes(initialRealm, input, home)
  assignTextures(biomeMap, initialRealm)
  val realm = simulation.Realm(
      boundary = initialRealm.boundary,
      nodes = initialRealm.nodes.map {
        simulation.Node(
            id = it.id,
            position = Vector3(it.position),
            height = it.height,
            isWalkable = it.isWalkable,
            biome = biomeMap[it.id]!!,
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
    assert(data.firstNode != data.secondNode)
    val data2 = simulation.FaceInfo(
        type = data.type,
        firstNode = getNode(data.firstNode?.id),
        secondNode = getNode(data.secondNode?.id),
        texture = data.texture,
        debugInfo = data.debugInfo
    )
    face.data = data2
    assert(data2.firstNode != data2.secondNode)
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