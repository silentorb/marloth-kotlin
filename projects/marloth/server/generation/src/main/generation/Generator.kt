package generation

import generation.abstract.closeDeadEnds
import generation.abstract.createTunnelNodes
import generation.abstract.handleOverlapping
import generation.abstract.unifyWorld
import generation.structure.generateStructure
import mythic.spatial.Vector3
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

  var index = 0
  for (node in world.graph.nodes) {
    node.index = index++
  }
}

//data class WorldBundle(val abstractWorld: AbstractWorld, val structureWorld: StructureWorld)

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
  return World(
      world,
      listOf(Player(0, world.nodes.first().position)))
}

fun generateWorld(input: WorldInput): World {
  val abstractWorld = AbstractWorld(input.boundary)
  generateAbstract(abstractWorld, input.dice)
  generateStructure(abstractWorld)
  return World(
      abstractWorld,
      listOf(Player(0, abstractWorld.nodes.first().position)))
}

fun generateDefaultWorld() = generateWorld(WorldInput(
    WorldBoundary(
        Vector3(-50f, -50f, -50f),
        Vector3(50f, 50f, 50f)
    ),
    Dice(2)
))