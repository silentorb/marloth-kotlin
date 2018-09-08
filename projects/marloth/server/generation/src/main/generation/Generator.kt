package generation

import colliding.Sphere
import generation.abstract.*
import generation.structure.doorwayLength
import generation.structure.generateStructure
import intellect.Pursuit
import intellect.Spirit
import mythic.sculpting.FlexibleFace
import mythic.spatial.*
import physics.Body
import randomly.Dice
import simulation.*
import simulation.changing.Instantiator

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

fun placeEnemy(realm: Realm, nextId: IdSource, dice: Dice): Hand {
  val node = dice.getItem(realm.locationNodes.drop(1))// Skip the node where the player starts
  val wall = dice.getItem(node.walls)
  val position = getVector3Center(Vector3(node.position), Vector3(wall.edges[0].first))
  return newCharacter(
      nextId = nextId,
      faction = 2,
      definition = characterDefinitions.monster,
      position = position,
      node = node,
      spirit = Spirit(
          id = 0,
          pursuit = Pursuit()
      )
  )
}

fun placeEnemies(realm: Realm, nextId: IdSource, dice: Dice, scale: Float): Deck {
//  val enemyCount = (10f * scale).toInt()
  val enemyCount = 1
  return toDeck((0 until enemyCount).map {
    placeEnemy(realm, nextId, dice)
  })
}

fun placeWallLamps(realm: Realm, nextId: IdSource, dice: Dice, scale: Float): Deck {
  val count = (10f * scale).toInt()
  val isValidLampWall = { it: FlexibleFace ->
    getFaceInfo(it).type == FaceType.wall && getFaceInfo(it).texture != null
  }
  val options = realm.locationNodes.filter { it.walls.any(isValidLampWall) }
  assert(options.any())
  val nodes = dice.getList(options, count)
  val hands = nodes.mapNotNull { node ->
    val options2 = node.walls.filter(isValidLampWall)
    if (options2.any()) {
      val wall = dice.getItem(options2)
      val edge = wall.edges[0]
      val position = getVector3Center(Vector3(edge.first), Vector3(edge.second)) +
          Vector3(0f, 0f, 0.9f) + wall.normal * -0.1f
//    val angle = getAngle(edge.first.copy().cross(edge.second).xy())
//    val angle = getAngle(wall.normal.xy())
      val angle = Quaternion().rotateTo(Vector3(1f, 0f, 0f), wall.normal)
//      instantiator.createWallLamp(position, node, angle)
      val id = nextId()
      Hand(
          body = Body(
              id = id,
              shape = Sphere(0.3f),
              position = position,
              orientation = angle,
              velocity = Vector3(),
              node = node,
              attributes = doodadBodyAttributes,
              gravity = false
          ),
          depiction = Depiction(
              id = id,
              type = DepictionType.wallLamp
          )
      )
    } else
      null
  }

  return toDeck(hands)
}

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y) / (100 * 100)

fun newPlayer(nextId: IdSource, playerNode: Node): Hand =
    newCharacter(
        nextId = nextId,
        faction = 1,
        definition = characterDefinitions.player,
        position = Vector3(playerNode.position),
        node = playerNode,
        player = Player(
            playerId = 1,
            character = 0,
            viewMode = ViewMode.firstPerson
        )
    )

fun generateWorld(input: WorldInput): World {
  val realm = Realm(input.boundary)
  val scale = calculateWorldScale(realm.boundary.dimensions)
  val tunnels = generateAbstract(realm, input.biomes, input.dice, scale)
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
      dice = Dice(2),
      biomes = createBiomes()
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