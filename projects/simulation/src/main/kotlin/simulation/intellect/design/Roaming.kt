package simulation.intellect.design

import silentorb.mythic.ent.Id
import silentorb.mythic.intellect.navigation.fromRecastVector3
import silentorb.mythic.spatial.Vector3
import simulation.intellect.Pursuit
import simulation.intellect.assessment.Knowledge
import simulation.intellect.execution.getPath
import simulation.intellect.navigation.nearestPolygon
import simulation.main.World

fun startRoaming(world: World, actor: Id, knowledge: Knowledge): Vector3? {
  val deck = world.deck
  val body = deck.bodies[actor]
  return if (body != null) {
    val dice = world.dice
    val range = 5f
    val base = dice.takeOne(deck.bodies.values).position

    val target = base + Vector3(
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
  } else
    null
}

fun updateRoamingTargetPosition(world: World, actor: Id, knowledge: Knowledge, pursuit: Pursuit): Vector3? {
  val definitions = world.definitions
  val character = world.deck.characters[actor]!!
  val characterDefinition = character.definition
  return if (characterDefinition.runSpeed == 0f)
    null
  else if (pursuit.targetPosition == null) // || !pathIsAccessible(world, knowledge, pursuit.path))
    startRoaming(world, actor, knowledge)
  else {
    val body = world.deck.bodies[actor]!!
    if (body.position.distance(pursuit.targetPosition) < 3f)
      startRoaming(world, actor, knowledge)
    else
      pursuit.targetPosition
  }
}
