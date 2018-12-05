package intellect.design

import intellect.Pursuit
import intellect.acessment.Knowledge
import intellect.acessment.getVisibleEnemies
import intellect.execution.isInAttackRange
import mythic.ent.Id

fun updateTargetEnemy(knowledge: Knowledge, pursuit: Pursuit): Id? {
  val visibleEnemies = getVisibleEnemies(knowledge.character, knowledge)
  return if (pursuit.targetEnemy != null && visibleEnemies.any { it.id == pursuit.targetEnemy })
    pursuit.targetEnemy
  else if (visibleEnemies.any())
    visibleEnemies.first().id
  else
    null
}

fun updatePursuit(knowledge: Knowledge, pursuit: Pursuit): Pursuit {
  val targetEnemy = updateTargetEnemy(knowledge, pursuit)
  val path = if (targetEnemy != null) {
    val bodies = knowledge.world.bodyTable
    val targetBody = bodies[targetEnemy]!!
    val attackerBody = bodies[knowledge.spiritId]!!
    val ability = knowledge.character.abilities[0]
    if (isInAttackRange(attackerBody, targetBody, ability))
      null
    else
      updateAttackMovementPath(knowledge, targetEnemy, pursuit.path)
  }
  else
    updateRoamingPath(knowledge, pursuit)

  return Pursuit(
      targetEnemy = targetEnemy,
      path = path
  )
}
