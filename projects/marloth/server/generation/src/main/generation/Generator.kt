package generation

import generation.abstract.closeDeadEnds
import generation.abstract.createTunnelNodes
import generation.abstract.handleOverlapping
import generation.abstract.unifyWorld
import generation.structure.generateStructure
import mythic.spatial.*
import org.joml.minus
import org.joml.plus
import org.joml.xy
import randomly.Dice
import scenery.DepictionType
import scenery.Light
import scenery.LightType
import simulation.*
import simulation.changing.Instantiator
import simulation.changing.InstantiatorConfig

fun createNode(abstractWorld: AbstractWorld, dice: Dice): Node {
  val radius = dice.getFloat(5f, 10f)
  val start = abstractWorld.boundary.start + radius
  val end = abstractWorld.boundary.end - radius
  val node = Node(
      Vector3(dice.getFloat(start.x, end.x), dice.getFloat(start.y, end.y), 0f),
      radius,
      NodeType.room
  )
  abstractWorld.nodes.add(node)
  return node
}

fun createNodes(count: Int, world: AbstractWorld, dice: Dice) {
  for (i in 0..count) {
    createNode(world, dice)
  }
}


fun generateAbstract(world: AbstractWorld, dice: Dice, scale: Float) {
  val nodeCount = (20 * scale).toInt()
  createNodes(nodeCount, world, dice)
  handleOverlapping(world.graph)
  unifyWorld(world.graph)
  closeDeadEnds(world.graph)
  createTunnelNodes(world)

  fillIndexes(world.graph)
}

fun fillIndexes(graph: NodeGraph) {
  var index = 0
  for (node in graph.nodes) {
    node.index = index++
  }
}

fun placeEnemy(world: World, instantiator: Instantiator, dice: Dice) {
  val node = dice.getItem(world.meta.nodes.drop(1))// Skip the node where the player starts
  val wall = dice.getItem(node.walls)
  val position = getVector3Center(node.position, wall.edges[0].first)
  instantiator.createAiCharacter(characterDefinitions.monster, world.factions[1], position)
}


fun placeEnemies(world: World, instantiator: Instantiator, dice: Dice, scale: Float) {
  val enemyCount = (10f * scale).toInt()
  for (i in 0 until enemyCount) {
    placeEnemy(world, instantiator, dice)
  }
}

fun placeWallLamps(world: World, instantiator: Instantiator, dice: Dice, scale: Float) {
  val count = (10f * scale).toInt()
  val nodes = dice.getList(world.meta.nodes, count)
  nodes.forEach { node ->
    val wall = dice.getItem(node.walls)
    val edge = wall.edges[0]
    val position = getVector3Center(edge.first, edge.second) +
        Vector3(0f, 0f, 0.9f) + wall.normal * -0.1f
//    val angle = getAngle(edge.first.copy().cross(edge.second).xy)
    val angle = getAngle(wall.normal.xy)
    instantiator.createWallLamp(position, Quaternion().rotateZ(angle))
  }
}

fun generateWorld(input: WorldInput, instantiatorConfig: InstantiatorConfig): World {
  val abstractWorld = AbstractWorld(input.boundary)
  val scale = (abstractWorld.boundary.dimensions.x * abstractWorld.boundary.dimensions.y) / (100 * 100)
  generateAbstract(abstractWorld, input.dice, scale)
  generateStructure(abstractWorld)
  val world = World(abstractWorld)
  val instantiator = Instantiator(world, instantiatorConfig)
  instantiator.createPlayer(1)
//  world.lights.add(Light(
//      type = LightType.point,
//      color = Vector4(1f, 1f, 1f, 1f),
//      position = world.meta.nodes[0].position + Vector3(0f, 0f, 1f),
//      direction = Vector4(0f, 0f, 0f, 15f)
//  ))
  placeEnemies(world, instantiator, input.dice, scale)
  placeWallLamps(world, instantiator, input.dice, scale)

  return world
}

fun generateDefaultWorld(instantiatorConfig: InstantiatorConfig) = generateWorld(WorldInput(
    WorldBoundary(
        Vector3(-50f, -50f, -50f),
        Vector3(50f, 50f, 50f)
    ),
    Dice(2)
), instantiatorConfig)