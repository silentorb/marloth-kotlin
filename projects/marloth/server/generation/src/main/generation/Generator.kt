package generation

import generation.abstract.closeDeadEnds
import generation.abstract.createTunnelNodes
import generation.abstract.handleOverlapping
import generation.abstract.unifyWorld
import generation.structure.generateStructure
import mythic.spatial.Vector3
import mythic.spatial.getVector3Center
import org.joml.minus
import org.joml.plus
import randomly.Dice
import simulation.*

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

fun generateAbstract(world: AbstractWorld, dice: Dice) {
  createNodes(20, world, dice)
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

fun createTestWorld(): World {
  val boundary = WorldBoundary(
      Vector3(-50f, -50f, -50f),
      Vector3(50f, 50f, 50f)
  )

  val first = Node(Vector3(-18f, 0f, 0f), 5f, NodeType.room)
  val second = Node(Vector3(18f, 0f, 0f), 5f, NodeType.room)
  val tunnel = Node(Vector3(0f, 0f, 0f), .5f, NodeType.tunnel)
  val world = AbstractWorld(boundary)
  world.nodes.add(first)
  world.nodes.add(second)
  world.nodes.add(tunnel)
  world.graph.connect(first, tunnel, ConnectionType.tunnel)
  world.graph.connect(second, tunnel, ConnectionType.tunnel)

  generateStructure(world)
  fillIndexes(world.graph)
  val result = World(world)
  result.createPlayer(1)
  return result
}

fun placeEnemy(world: World, dice: Dice) {
  val node = dice.getItem(world.meta.nodes)
  val wall = dice.getItem(node.walls)
  val position = getVector3Center(node.position, wall.edges[0].first)
  world.createCharacter(position, 100)
}

fun placeEnemies(world: World, dice: Dice) {
  val enemyCount = 10
  for (i in 0 until enemyCount) {
    placeEnemy(world, dice)
  }
}

fun generateWorld(input: WorldInput): World {
  val abstractWorld = AbstractWorld(input.boundary)
  generateAbstract(abstractWorld, input.dice)
  generateStructure(abstractWorld)
  val world = World(abstractWorld)
  world.createPlayer(1)
  placeEnemies(world, input.dice)
  return world
}

fun generateDefaultWorld() = generateWorld(WorldInput(
    WorldBoundary(
        Vector3(-50f, -50f, -50f),
        Vector3(50f, 50f, 50f)
    ),
    Dice(2)
))