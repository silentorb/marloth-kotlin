package simulation.intellect.execution

import mythic.ent.Id
import simulation.intellect.Path
import simulation.intellect.acessment.Knowledge
import mythic.spatial.Vector3
import simulation.main.World
import simulation.input.Command
import simulation.input.CommandType
import simulation.input.Commands
import simulation.misc.Graph

fun doorwayPosition(graph: Graph, firstNode: Id, secondNode: Id): Vector3 {
  val a = graph.nodes[firstNode]!!
  val b = graph.nodes[secondNode]!!
  val vector = (b.position - a.position).normalize()
  return a.position + vector * a.radius
}

fun getPathTargetPosition(world: World, knowledge: Knowledge, path: Path): Vector3 {
  val graph = world.realm.graph
  val body = world.deck.bodies[knowledge.spiritId]!!
  val doorway = doorwayPosition(graph, body.nearestNode, path.first())
  val horizontalDistance = body.position.copy(z = 0f).distance(doorway.copy(z = 0f))
  println("dist " + horizontalDistance + " " + body.nearestNode)
  return if (horizontalDistance > 0.5f)
    doorway
  else
    graph.nodes[path.first()]!!.position
}

fun moveStraightTowardPosition(world: World, knowledge: Knowledge, target: Vector3): Commands {
  val body = world.deck.bodies[knowledge.spiritId]!!
  val position = body.position
  val offset = (target - position).normalize()
  return spiritNeedsFacing(world, knowledge, offset, 0.1f) {
    listOf(Command(CommandType.moveUp, knowledge.spiritId))
  }
}

fun moveSpirit(world: World, knowledge: Knowledge, path: Path): Commands =
    moveStraightTowardPosition(world, knowledge, getPathTargetPosition(world, knowledge, path))
