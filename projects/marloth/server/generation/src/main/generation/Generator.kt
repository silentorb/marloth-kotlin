package generation

import generation.abstract.*
import generation.structure.doorwayLength
import generation.structure.generateStructure
import intellect.NewSpirit
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
import simulation.changing.NewEntities
import simulation.changing.createNewEntitiesWorld

fun createRoomNode(abstractWorld: AbstractWorld, biomes: List<Biome>, dice: Dice): Node {
  val radius = dice.getFloat(5f, 10f)
  val start = abstractWorld.boundary.start + radius
  val end = abstractWorld.boundary.end - radius

  val node = Node(
      position = Vector3(dice.getFloat(start.x, end.x), dice.getFloat(start.y, end.y), 0f),
      radius = radius,
      biome = dice.getItem(biomes),
      isSolid = false,
      isWalkable = true
  )
  abstractWorld.nodes.add(node)
  return node
}

fun createRoomNodes(count: Int, world: AbstractWorld, biomes: List<Biome>, dice: Dice) {
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

fun generateAbstract(world: AbstractWorld, biomes: List<Biome>, dice: Dice, scale: Float): List<Node> {
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

fun placeEnemy(world: World, nextId: IdSource, dice: Dice): NewCharacter {
  val node = dice.getItem(world.meta.locationNodes.drop(1))// Skip the node where the player starts
  val wall = dice.getItem(node.walls)
  val position = getVector3Center(node.position, wall.edges[0].first)
//  instantiator.createAiCharacter(characterDefinitions.monster, world.factions[1], position, node)
  val id = nextId()
  return NewCharacter(
      id = id,
      faction = 2,
      definition = characterDefinitions.monster,
      abilities = characterDefinitions.monster.abilities.map { NewAbility(nextId()) },
      position = position,
      node = node,
      spirit = NewSpirit(id)
  )
}

fun placeEnemies(world: World, nextId: IdSource, dice: Dice, scale: Float): List<NewCharacter> {
//  val enemyCount = (10f * scale).toInt()
  val enemyCount = 1
  return (0 until enemyCount).map {
    placeEnemy(world, nextId, dice)
  }
}

fun placeWallLamps(world: WorldMap, instantiator: Instantiator, dice: Dice, scale: Float) {
  val count = (10f * scale).toInt()
  val isValidLampWall = { it: FlexibleFace ->
    getFaceInfo(it).type == FaceType.wall && getFaceInfo(it).texture != null
  }
  val options = world.meta.locationNodes.filter { it.walls.any(isValidLampWall) }
  assert(options.any())
  val nodes = dice.getList(options, count)
  nodes.forEach { node ->
    val options = node.walls.filter(isValidLampWall)
    if (options.any()) {
      val wall = dice.getItem(options)
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
  val abstractWorld = AbstractWorld(input.boundary)
  val scale = calculateWorldScale(abstractWorld.boundary.dimensions)
  val tunnels = generateAbstract(abstractWorld, input.biomes, input.dice, scale)
  generateStructure(abstractWorld, input.dice, tunnels)
  val playerNode = abstractWorld.nodes.first()
  var id = 1
  val nextId = newIdSource(1)
  val newEntities = NewEntities(
      newCharacters = listOf(
          NewCharacter(
              id = nextId(),
              faction = 1,
              definition = characterDefinitions.player,
              abilities = characterDefinitions.player.abilities.map { NewAbility(nextId()) },
              position = playerNode.position,
              node = playerNode,
              spirit = null
          )
      )
  )
  val world = World(
      factions = listOf(
          Faction(1, "Misfits"),
          Faction(2, "Monsters")
      ),
      meta = abstractWorld,
      nextId = nextId(),
      players = listOf(Player(newEntities.newCharacters.first().id, 1, instantiatorConfig.defaultPlayerView))
  ).plus(createNewEntitiesWorld(newEntities))
//  instantiator.createPlayer(1)

//  placeWallLamps(world, instantiator, input.dice, scale)

  return world
}

fun generateDefaultWorld(instantiatorConfig: InstantiatorConfig, biomes: List<Biome>) = generateWorld(WorldInput(
    boundary = WorldBoundary(
        Vector3(-50f, -50f, -50f),
        Vector3(50f, 50f, 50f)
    ),
    dice = Dice(2),
    biomes = biomes), instantiatorConfig)