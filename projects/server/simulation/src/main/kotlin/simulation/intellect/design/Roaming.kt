package simulation.intellect.design

import silentorb.mythic.ent.Id
import silentorb.mythic.intellect.navigation.fromRecastVector3
import silentorb.mythic.spatial.Vector3
import simulation.intellect.Path
import simulation.intellect.Pursuit
import simulation.intellect.assessment.Knowledge
import simulation.intellect.execution.getPath
import simulation.intellect.execution.getPathTargetPosition
import simulation.intellect.navigation.nearestPolygon
import simulation.main.World
import simulation.misc.*

fun isCellExplorableByMonsters(attributes: Set<CellAttribute>): Boolean =
    attributes.contains(CellAttribute.traversable)
        && !attributes.contains(CellAttribute.home)

fun startRoamingOld(world: World, character: Id, knowledge: Knowledge): Vector3? {
  val body = world.deck.bodies[character]!!
  val currentCell = getPointCell(body.position)
//  val options = knowledge.grid.cells
  val options = world.realm.grid.cells
      .filter { isCellExplorableByMonsters(it.value.attributes) && it.value.slots.any() }
      .keys.minus(currentCell)
  return if (options.none())
    null
  else {
    val destination = world.dice.takeOne(options)
    val destinationCell = world.realm.grid.cells[destination]!!
    world.dice.takeOne(cellSlots(destination, destinationCell))
  }
}

fun startRoaming(world: World, actor: Id, knowledge: Knowledge): Vector3? {
  val post = world.deck.spirits[actor]?.post
  val body = world.deck.bodies[actor]
  return if (post != null && body != null) {
    val dice = world.dice
    val range = 5f
    val target = post + Vector3(
        dice.getFloat(-range, range),
        dice.getFloat(-range, range),
        dice.getFloat(-range, range)
    )
    val polygon = nearestPolygon(world.navigation!!, target)
    if (polygon != null) {
      val point = fromRecastVector3(polygon.result.nearestPos)
      val path = getPath(world, actor, point)
      if (path.any() && fromRecastVector3(path.last().pos).distance(target) < 1f)
        point
      else
        null
    } else
      null
//    val path = getPath(world, actor, target)
//    if (path.any()) {
//      val last = fromRecastVector3(path.last().pos)
//      if (last.distance(target) < 1f)
//        last
//      else
//        null
//    } else
//      null
  } else
    null
}

fun getRemainingPath(node: Id, path: Path): Path {
  val index = path.indexOf(node)
  return if (index == -1)
    path
  else
    path.drop(index + 1)
}

fun updateRoamingTargetPosition(world: World, actor: Id, knowledge: Knowledge, pursuit: Pursuit): Vector3? {
  val definitions = world.definitions
  val character = world.deck.characters[actor]!!
  val characterDefinition = character.definition
  return if (characterDefinition.speed == 0f)
    null
  else if (pursuit.targetPosition == null) // || !pathIsAccessible(world, knowledge, pursuit.path))
    startRoaming(world, actor, knowledge)
  else {
    val body = world.deck.bodies[actor]!!
    if (body.position.distance(pursuit.targetPosition) < 2f)
      startRoaming(world, actor, knowledge)
    else
      pursuit.targetPosition
  }
}
