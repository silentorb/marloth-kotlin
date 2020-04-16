package simulation.intellect.design

import silentorb.mythic.ent.Id
import silentorb.mythic.spatial.Vector3
import silentorb.mythic.randomly.Dice
import simulation.intellect.Path
import simulation.intellect.Pursuit
import simulation.intellect.assessment.Knowledge
import simulation.main.World
import simulation.misc.*

fun isCellExplorableByMonsters(attributes: Set<CellAttribute>): Boolean =
    attributes.contains(CellAttribute.traversable)
        && !attributes.contains(CellAttribute.home)

fun startRoaming(world: World, character: Id, knowledge: Knowledge): Vector3? {
  val body = world.deck.bodies[character]!!
  val currentCell = getPointCell(body.position)
  val options = knowledge.grid.cells
      .filter { isCellExplorableByMonsters(it.value.attributes) }
      .keys.minus(currentCell)
  val destination = Dice.global.takeOne(options)
  return absoluteCellPosition(destination) + floorOffset
}

fun getRemainingPath(node: Id, path: Path): Path {
  val index = path.indexOf(node)
  return if (index == -1)
    path
  else
    path.drop(index + 1)
}

fun updateRoamingTargetPosition(world: World, character: Id, knowledge: Knowledge, pursuit: Pursuit): Vector3? {
  return if (world.deck.characters[character]!!.definition.maxSpeed == 0f)
    null
  else if (pursuit.targetPosition == null) // || !pathIsAccessible(world, knowledge, pursuit.path))
    startRoaming(world, character, knowledge)
  else {
    val body = world.deck.bodies[character]!!
    if (body.position.distance(pursuit.targetPosition) < cellHalfLength)
      startRoaming(world, character, knowledge)
    else
      pursuit.targetPosition
  }
}

fun updateAttackMovementPath(world: World, character: Id, knowledge: Knowledge, targetEnemy: Id, path: Path?): Path? {
  return if (path == null) { // || !pathIsAccessible(world, knowledge, path)) {
    val bodies = world.deck.bodies
    val body = bodies[character]!!
    val target = knowledge.characters[targetEnemy]!!
    if (body.nearestNode == target.nearestNode)
      null
    else
      findPath(world.realm, body.nearestNode, target.nearestNode)
  } else {
    val body = world.deck.bodies[character]!!
    val remainingPath = getRemainingPath(body.nearestNode, path)
    if (remainingPath.any())
      remainingPath
    else
      null
  }
}
