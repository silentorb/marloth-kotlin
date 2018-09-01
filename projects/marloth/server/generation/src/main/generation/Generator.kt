package generation

import generation.abstract.*
import generation.structure.doorwayLength
import generation.structure.generateStructure
import intellect.NewSpirit
import intellect.Pursuit
import intellect.Spirit
import mythic.sculpting.FlexibleFace
import mythic.spatial.Quaternion
import mythic.spatial.Vector3
import mythic.spatial.getVector3Center
import mythic.spatial.times
import org.joml.minus
import org.joml.plus
import randomly.Dice
import simulation.*
import simulation.changing.Instantiator
import simulation.changing.InstantiatorConfig

fun createRoomNode(realm: Realm, biomes: List<Biome>, dice: Dice): Node {
  val radius = dice.getFloat(5f, 10f)
  val start = realm.boundary.start + radius
  val end = realm.boundary.end - radius

  val node = Node(
      position = Vector3(dice.getFloat(start.x, end.x), dice.getFloat(start.y, end.y), 0f),
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
  val position = getVector3Center(node.position, wall.edges[0].first)
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

fun placeWallLamps(world: World, instantiator: Instantiator, dice: Dice, scale: Float) {
  val count = (10f * scale).toInt()
  val isValidLampWall = { it: FlexibleFace ->
    getFaceInfo(it).type == FaceType.wall && getFaceInfo(it).texture != null
  }
  val options = world.realm.locationNodes.filter { it.walls.any(isValidLampWall) }
  assert(options.any())
  val nodes = dice.getList(options, count)
  nodes.forEach { node ->
    val options2 = node.walls.filter(isValidLampWall)
    if (options2.any()) {
      val wall = dice.getItem(options2)
      val edge = wall.edges[0]
      val position = getVector3Center(edge.first, edge.second) +
          Vector3(0f, 0f, 0.9f) + wall.normal * -0.1f
//    val angle = getAngle(edge.first.copy().cross(edge.second).xy())
//    val angle = getAngle(wall.normal.xy())
      val angle = Quaternion().rotateTo(Vector3(1f, 0f, 0f), wall.normal)
//      instantiator.createWallLamp(position, node, angle)
    }
  }
}

fun calculateWorldScale(dimensions: Vector3) =
    (dimensions.x * dimensions.y) / (100 * 100)

fun generateWorld(input: WorldInput, instantiatorConfig: InstantiatorConfig): World {
  val realm = Realm(input.boundary)
  val scale = calculateWorldScale(realm.boundary.dimensions)
  val tunnels = generateAbstract(realm, input.biomes, input.dice, scale)
  generateStructure(realm, input.dice, tunnels)
  val playerNode = realm.nodes.first()
  var id = 1
  val nextId = newIdSource(1)
  val newEntities = toDeck(newCharacter(
      nextId = nextId,
      faction = 1,
      definition = characterDefinitions.player,
      position = playerNode.position,
      node = playerNode,
      player = Player(
          playerId = 1,
          character = 0,
          viewMode = instantiatorConfig.defaultPlayerView
      )
  ))
  val deck = Deck(
      factions = listOf(
          Faction(1, "Misfits"),
          Faction(2, "Monsters")
      )
  ).plus(newEntities)
//  instantiator.createPlayer(1)

//  placeWallLamps(world, instantiator, input.dice, scale)

  return World(
      deck = deck,
      tables = toTables(deck),
      nextId = nextId(),
      realm = realm)
}

fun generateDefaultWorld(instantiatorConfig: InstantiatorConfig, biomes: List<Biome>) = generateWorld(WorldInput(
    boundary = WorldBoundary(
        Vector3(-50f, -50f, -50f),
        Vector3(50f, 50f, 50f)
    ),
    dice = Dice(2),
    biomes = biomes), instantiatorConfig)