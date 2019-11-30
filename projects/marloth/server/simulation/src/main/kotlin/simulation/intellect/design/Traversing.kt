package simulation.intellect.design

import simulation.intellect.Path
import simulation.intellect.Pursuit
import simulation.intellect.acessment.Knowledge
import mythic.ent.Id
import mythic.spatial.Vector3
import mythic.spatial.toVector3
import randomly.Dice
import simulation.main.World
import simulation.misc.cellLength
import simulation.misc.getPointCell

//fun getNextPathNode(world: World, knowledge: Knowledge, path: Path): Id? {
//  val body = world.deck.bodies[knowledge.spiritId]!!
//  val nextNode = path.first()
//  return nextNode
//}
//
//fun pathIsAccessible(world: World, knowledge: Knowledge, path: Path): Boolean =
//    getNextPathFace(world, knowledge, path) != null

//fun startRoamingPath(world: World, knowledge: Knowledge): Path? {
//  val body = world.deck.bodies[knowledge.spiritId]!!
//  val options = knowledge.nodes
//      .filter {
//        val node = world.realm.nodeTable[it]!!
//        node.id != body.nearestNode
//      }
//
//  val destination = Dice.global.takeOne(options)
//  val path = findPath(world.realm, body.nearestNode, destination)
////  assert(path != null)
////  assert(path!!.any())
////  assert(pathIsAccessible(world, knowledge, path))
//  return path
//}

fun startRoaming(world: World, knowledge: Knowledge): Vector3? {
  val body = world.deck.bodies[knowledge.spiritId]!!
  val currentCell = getPointCell(body.position)
  val options = knowledge.grid.cells.keys.minus(currentCell)
  val destination = Dice.global.takeOne(options)
  return destination.toVector3() + cellLength / 2f
}

fun getRemainingPath(node: Id, path: Path): Path {
  val index = path.indexOf(node)
  return if (index == -1)
    path
  else
    path.drop(index + 1)
}

//fun updateRoamingPath(world: World, knowledge: Knowledge, pursuit: Pursuit): Path? {
//  return if (pursuit.path == null) // || !pathIsAccessible(world, knowledge, pursuit.path))
//    startRoamingPath(world, knowledge)
//  else {
//    val body = world.deck.bodies[knowledge.spiritId]!!
//    val remainingPath = getRemainingPath(body.nearestNode, pursuit.path)
//    if (remainingPath.any())
//      remainingPath
//    else
//      startRoamingPath(world, knowledge)
//  }
//}

fun updateRoamingTargetPosition(world: World, knowledge: Knowledge, pursuit: Pursuit): Vector3? {
  return if (pursuit.targetPosition == null) // || !pathIsAccessible(world, knowledge, pursuit.path))
    startRoaming(world, knowledge)
  else {
    val body = world.deck.bodies[knowledge.spiritId]!!
    if (body.position.distance(pursuit.targetPosition) < 1f)
      startRoaming(world, knowledge)
    else
      pursuit.targetPosition
  }
}

fun updateAttackMovementPath(world: World, knowledge: Knowledge, targetEnemy: Id, path: Path?): Path? {
  return if (path == null) { // || !pathIsAccessible(world, knowledge, path)) {
    val bodies = world.deck.bodies
    val body = bodies[knowledge.spiritId]!!
    val target = knowledge.characters[targetEnemy]!!
    if (body.nearestNode == target.nearestNode)
      null
    else
      findPath(world.realm, body.nearestNode, target.nearestNode)
  } else {
    val body = world.deck.bodies[knowledge.spiritId]!!
    val remainingPath = getRemainingPath(body.nearestNode, path)
    if (remainingPath.any())
      remainingPath
    else
      null
  }
}
