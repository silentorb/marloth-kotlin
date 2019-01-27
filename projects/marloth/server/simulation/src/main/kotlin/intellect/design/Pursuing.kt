package intellect.design

import intellect.Pursuit
import intellect.acessment.Knowledge
import intellect.acessment.character
import intellect.acessment.getVisibleEnemies
import intellect.execution.spiritAttackRangeBuffer
import mythic.ent.Id
import physics.isInVoid
import simulation.World

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
  if (isInVoid(world, knowledge.spiritId))
    return Pursuit()

  val targetEnemy = updateTargetEnemy(world, knowledge, pursuit)
  val target = knowledge.characters[pursuit.targetEnemy]
  val (path, targetPosition) = if (target != null) {
    val bodies = world.bodyTable
    val attackerBody = bodies[knowledge.spiritId]!!
    val ability = character(world, knowledge).abilities[0]
    val range = ability.definition.range - spiritAttackRangeBuffer
    val distance = attackerBody.position.distance(target.position)
    val gap = distance - range
    if (gap > 0f && attackerBody.node == target.node)
      Pair(null, (target.position - attackerBody.position).normalize() * gap)
    else
      Pair(updateAttackMovementPath(world, knowledge, target.id, pursuit.path), null)
  } else
    Pair(updateRoamingPath(world, knowledge, pursuit), null)

  return Pursuit(
      targetEnemy = targetEnemy,
      path = path,
      targetPosition = targetPosition
  )
}
