package intellect.design

import intellect.Path
import intellect.Pursuit
import intellect.acessment.Knowledge
import mythic.ent.Id
import randomly.Dice
import simulation.*

fun getNextPathFace(knowledge: Knowledge, path: Path): Id? {
  val body = knowledge.world.bodyTable[knowledge.spiritId]!!
  val faces = knowledge.world.realm.faces
  val node = knowledge.world.realm.nodeTable[body.node]!!
  val nextNode = path.first()
  val nodeFaces = node.walls.map { faces[it]!! }
  return node.walls.firstOrNull { getOtherNode(node, faces[it]!!) == nextNode }
}

fun pathIsAccessible(knowledge: Knowledge, path: Path): Boolean =
    getNextPathFace(knowledge, path) != null

fun startRoaming(knowledge: Knowledge): Path {
  val body = knowledge.world.bodyTable[knowledge.spiritId]!!
  val options = knowledge.nodes
      .filter {
        val node = knowledge.world.realm.nodeTable[it]!!
        node.id != body.node && node.isWalkable
      }

  val destination = Dice.global.getItem(options)
  val path = findPath(knowledge.world.realm, body.node, destination)
  assert(path != null)
  assert(path!!.any())
  assert(pathIsAccessible(knowledge, path))
  return path
}

fun getRemainingPath(node: Id, path: Path): Path {
  val index = path.indexOf(node)
  return if (index == -1)
    path
  else
    path.drop(index + 1)
}

fun updateRoamingPath(knowledge: Knowledge, pursuit: Pursuit): Path {
  return if (pursuit.path == null || !pathIsAccessible(knowledge, pursuit.path))
    startRoaming(knowledge)
  else {
    val body = knowledge.world.bodyTable[knowledge.spiritId]!!
    val remainingPath = getRemainingPath(body.node, pursuit.path)
    if (remainingPath.any())
      remainingPath
    else
      startRoaming(knowledge)
  }
}

fun updateAttackMovementPath(knowledge: Knowledge, targetEnemy: Id, path: Path?): Path? {
  return if (path == null || !pathIsAccessible(knowledge, path)) {
    val bodies = knowledge.world.bodyTable
    val body = bodies[knowledge.spiritId]!!
    val targetBody = bodies[targetEnemy]!!
    if (body.node == targetBody.node)
      null
    else
      findPath(knowledge.world.realm, body.node, targetBody.node)
  } else {
    val body = knowledge.world.bodyTable[knowledge.spiritId]!!
    val remainingPath = getRemainingPath(body.node, path)
    if (remainingPath.any())
      remainingPath
    else
      null
  }
}
