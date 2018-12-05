package intellect.execution

import intellect.Path
import intellect.acessment.Knowledge
import intellect.design.*
import mythic.spatial.Vector3
import simulation.Command
import simulation.CommandType
import simulation.Commands
import simulation.getFloor

fun getPathTargetPosition(knowledge: Knowledge, path: Path): Vector3 {
  val body = knowledge.world.bodyTable[knowledge.spiritId]!!
  val face = getNextPathFace(knowledge, path)
  if (face == null) {
    getNextPathFace(knowledge, path)
    throw Error("Not supported")
  }

  val edge = getFloor(knowledge.world.realm.mesh.faces[face]!!)
  val position = body.position
  val nearestPoint = edge.vertices.sortedBy { it.distance(position) }.first()
  return (edge.middle + nearestPoint) / 2f
}

fun moveSpirit(knowledge: Knowledge, target: Vector3): Commands {
  val body = knowledge.world.bodyTable[knowledge.spiritId]!!
  val position = body.position
  val offset = (target - position).normalize()
  return spiritNeedsFacing(knowledge, offset, 0.1f) {
    listOf(Command(CommandType.moveUp, knowledge.spiritId))
  }
}

fun moveSpirit(knowledge: Knowledge, path: Path): Commands =
    moveSpirit(knowledge, getPathTargetPosition(knowledge, path))
