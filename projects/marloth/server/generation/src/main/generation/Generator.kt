package generation

import gatherNodes
import generation.abstract.*
import generation.abstract.OldRealm
import generation.structure.assignTextures
import generation.structure.doorwayLength
import generation.structure.generateStructure
import generation.structure.idSourceFromNodes
import mythic.ent.Id
import mythic.spatial.*
import randomly.Dice
import simulation.*

fun createRoomNode(boundary: WorldBoundary, id: Id, dice: Dice): Node {
  val radius = dice.getFloat(5f, 10f)
  val start = boundary.start + radius
  val end = boundary.end - radius

  return Node(
      id = id,
      position = Vector3(dice.getFloat(start.x, end.x), dice.getFloat(start.y, end.y), 0f),
      radius = radius,
      isSolid = false,
      isWalkable = true,
      biome = Biome.void,
      height = 0f,
      floors = mutableListOf(),
      ceilings = mutableListOf(),
      walls = mutableListOf()
  )
}

fun createRoomNodes(boundary: WorldBoundary, count: Int, dice: Dice) =
    (1L..count).map { id ->
      createRoomNode(boundary, id, dice)
    }

fun getTwinTunnels(graph: Graph, tunnels: List<PreTunnel>): List<PreTunnel> =
    crossMap(tunnels.asSequence()) { a: PreTunnel, b: PreTunnel ->
      //      println("" + a.neighbors.any { b.neighbors.contains(it) } + ", " + a.position.distance(b.position))
      val c = a.connection.nodes(graph).any { b.connection.nodes(graph).contains(it) }
          && a.position.distance(b.position) < doorwayLength * 2f
//      println(c)
      c
    }

fun generateAbstract(boundary: WorldBoundary, dice: Dice, scale: Float): Pair<Graph, List<Node>> {
  val nodeCount = (20 * scale).toInt()
  val initialNodes = createRoomNodes(boundary, nodeCount, dice)
  val graph = handleOverlapping(initialNodes)
  val unifyingConnections = unifyWorld(graph)
  val secondConnections = graph.connections.plus(unifyingConnections)
  val deadEndClosingConnections = closeDeadEnds(graph.copy(connections = secondConnections))
  val thirdGraph = graph.copy(connections = secondConnections.plus(deadEndClosingConnections))

  val preTunnels = prepareTunnels(thirdGraph)
  val twinTunnels = getTwinTunnels(thirdGraph, preTunnels)
  val tunnelGraph = createTunnelNodes(thirdGraph, preTunnels.minus(twinTunnels))

  val fourthGraph = thirdGraph.plus(tunnelGraph).minusConnections(preTunnels.plus(twinTunnels).map { it.connection })
  return Pair(fourthGraph, tunnelGraph.nodes)
}

//fun fillIndexes(graph: Graph) {
//  var index = 1L
//  for (node in graph.nodes) {
//    node.id = index++
//  }
//}

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y) / (100 * 100)

fun getHome(graph: Graph): List<Node> {
  val start = getDeadEnds(graph).first()
  return gatherNodes(listOf(start)) { node ->
    node.neighbors(graph).filter { it.isWalkable && it.getConnection(graph, node)!!.type == ConnectionType.union }.toList()
  }
}

fun generateWorld(input: WorldInput): World {
  val scale = calculateWorldScale(input.boundary.dimensions)
  val (graph, tunnels) = generateAbstract(input.boundary, input.dice, scale)
  val nextId = idSourceFromNodes(graph.nodes)
  val home = getHome(graph)
  val initialRealm = OldRealm(
      graph = graph,
      nextId = nextId
  )
  val nextFaceId = newIdSource(1)
  val structuredRealm = generateStructure(initialRealm, nextFaceId, input.dice, tunnels)
  val biomeMap = assignBiomes(structuredRealm.nodes.values, input, home)
  val texturedFaces = assignTextures(structuredRealm.nodes, structuredRealm.connections, biomeMap)

//  val faces = initialRealm.mesh.faces.map {
//    val data = initialRealm.faces[it.id]!!
//    ConnectionFace(
//        faceType = data.faceType,
//        firstNode = data.firstNode,
//        secondNode = data.secondNode,
//        texture = data.texture,
//        debugInfo = data.debugInfo
//    )
//  }
  val realm = simulation.Realm(
      boundary = input.boundary,
      nodeList = structuredRealm.nodes.values.map {
        it.copy(
            biome = biomeMap[it.id]!!
        )
      },
      faces = texturedFaces,
      mesh = structuredRealm.mesh
  )
  val n = realm.locationNodes.first()
  val n2 = n.walls.map { realm.faces[it.id]!! }
//  val getNode = { id: Long? -> realm.nodeList.firstOrNull { it.id == id } }
//  realm.mesh.faces.forEach { face ->
//    val data = getFaceInfo(face)
//    assert(data.firstNode != data.secondNode)
//    val data2 = simulation.ConnectionFace(
//        faceType = data.faceType,
//        firstNode = getNode(data.firstNode?.id),
//        secondNode = getNode(data.secondNode?.id),
//        texture = data.texture,
//        debugInfo = data.debugInfo
//    )
//    face.data = data2
//    assert(data2.firstNode != data2.secondNode)
//  }
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