package generation

import generation.abstract.*
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
      radius
  )
  abstractWorld.nodes.add(node)
  return node
}

fun createNodes(count: Int, abstractWorld: AbstractWorld, dice: Dice) {
  for (i in 0..count) {
    createNode(abstractWorld, dice)
  }
}

fun generateAbstract(abstractWorld: AbstractWorld, dice: Dice) {
  createNodes(20, abstractWorld, dice)
  handleOverlapping(abstractWorld)
  unifyWorld(abstractWorld)
  closeDeadEnds(abstractWorld)
}

//data class WorldBundle(val abstractWorld: AbstractWorld, val structureWorld: StructureWorld)

fun generateWorld(input: WorldInput): World {
  val abstractWorld = AbstractWorld(input.boundary)
  generateAbstract(abstractWorld, input.dice)
  val structureWorld = StructureWorld()
  val groups = generateStructure(abstractWorld, structureWorld)
  return World(MetaWorld(abstractWorld, structureWorld, groups),
      listOf(Player(0, abstractWorld.nodes.first().position)))
}

fun generateDefaultWorld() = generateWorld(WorldInput(
    WorldBoundary(
        Vector3(-50f, -50f, -50f),
        Vector3(50f, 50f, 50f)
    ),
    Dice(2)
))