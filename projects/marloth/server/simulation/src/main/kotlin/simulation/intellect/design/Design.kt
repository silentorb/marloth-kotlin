package simulation.intellect.design

import simulation.intellect.Pursuit
import simulation.intellect.acessment.Knowledge
import simulation.intellect.acessment.character
import simulation.intellect.acessment.getVisibleEnemies
import simulation.intellect.execution.spiritAttackRangeBuffer
import mythic.ent.Id
import simulation.main.World

fun updateTargetEnemy(world: World, knowledge: Knowledge, pursuit: Pursuit): Id? {
  val visibleEnemies = getVisibleEnemies(character(world, knowledge), knowledge)
  return if (pursuit.targetEnemy != null && visibleEnemies.any { it.id == pursuit.targetEnemy })
    pursuit.targetEnemy
  else if (visibleEnemies.any())
    visibleEnemies.first().id
  else
    null
}

fun updatePursuit(world: World, knowledge: Knowledge, pursuit: Pursuit): Pursuit {
  val targetEnemy = updateTargetEnemy(world, knowledge, pursuit)
  val target = knowledge.characters[pursuit.targetEnemy]
  val (path, targetPosition) = if (target != null) {
    val bodies = world.deck.bodies
    val attackerBody = bodies[knowledge.spiritId]!!
    val ability = character(world, knowledge).abilities[0]
    val range = ability.definition.range - spiritAttackRangeBuffer
    val distance = attackerBody.position.distance(target.position)
    val gap = distance - range
    if (gap > 0f && attackerBody.nearestNode == target.nearestNode)
      Pair(null, (target.position - attackerBody.position).normalize() * gap)
    else
      Pair(updateAttackMovementPath(world, knowledge, target.id, pursuit.path), null)
  } else if (false) {
    // Guard room
    Pair(null, null)
  } else
    Pair(updateRoamingPath(world, knowledge, pursuit), null)

  return Pursuit(
      targetEnemy = targetEnemy,
      path = path,
      targetPosition = targetPosition
  )
}
