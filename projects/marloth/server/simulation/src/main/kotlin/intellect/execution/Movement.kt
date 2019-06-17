package intellect.execution

import intellect.Path
import intellect.acessment.Knowledge
import intellect.design.*
import mythic.spatial.Vector3
import simulation.*

fun getPathTargetPosition(world: World, knowledge: Knowledge, path: Path): Vector3 {
  throw Error("No longer implemented")
//  val body = world.bodyTable[knowledge.spiritId]!!
//  val face = getNextPathFace(world, knowledge, path)
//  if (face == null) {
//    getNextPathFace(world, knowledge, path)
//    throw Error("Not supported")
//  }
//
//  val edge = getFloor(world.realm.mesh.faces[face]!!)
//  val position = body.position
//  val nearestPoint = edge.vertices.sortedBy { it.distance(position) }.first()
//  return (edge.middle + nearestPoint) / 2f
}

fun moveStraightTowardPosition(world: World, knowledge: Knowledge, target: Vector3): Commands {
  val body = world.bodyTable[knowledge.spiritId]!!
  val position = body.position
  val offset = (target - position).normalize()
  return spiritNeedsFacing(world,knowledge, offset, 0.1f) {
    listOf(Command(CommandType.moveUp, knowledge.spiritId))
  }
}

fun moveSpirit(world: World, knowledge: Knowledge, path: Path): Commands =
    moveStraightTowardPosition(world, knowledge, getPathTargetPosition(world, knowledge, path))
