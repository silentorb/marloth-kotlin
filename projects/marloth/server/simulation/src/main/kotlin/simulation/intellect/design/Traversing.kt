package simulation.intellect.design

import simulation.intellect.Path
import simulation.intellect.Pursuit
import simulation.intellect.acessment.Knowledge
import mythic.ent.Id
import randomly.Dice
import simulation.main.World
import simulation.misc.getOtherNode

//fun getNextPathNode(world: World, knowledge: Knowledge, path: Path): Id? {
//  val body = world.deck.bodies[knowledge.spiritId]!!
//  val nextNode = path.first()
//  return nextNode
//}
//
//fun pathIsAccessible(world: World, knowledge: Knowledge, path: Path): Boolean =
//    getNextPathFace(world, knowledge, path) != null

fun startRoaming(world: World, knowledge: Knowledge): Path? {
  val body = world.deck.bodies[knowledge.spiritId]!!
  val options = knowledge.nodes
      .filter {
        val node = world.realm.nodeTable[it]!!
        node.id != body.nearestNode
      }

  val destination = Dice.global.takeOne(options)
  val path = findPath(world.realm, body.nearestNode, destination)
//  assert(path != null)
//  assert(path!!.any())
//  assert(pathIsAccessible(world, knowledge, path))
  return path
}

fun getRemainingPath(node: Id, path: Path): Path {
  val index = path.indexOf(node)
  return if (index == -1)
    path
  else
    path.drop(index + 1)
}

fun updateRoamingPath(world: World, knowledge: Knowledge, pursuit: Pursuit): Path? {
  return if (pursuit.path == null) // || !pathIsAccessible(world, knowledge, pursuit.path))
    startRoaming(world, knowledge)
  else {
    val body = world.deck.bodies[knowledge.spiritId]!!
    val remainingPath = getRemainingPath(body.nearestNode, pursuit.path)
    if (remainingPath.any())
      remainingPath
    else
      startRoaming(world, knowledge)
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
