package intellect.design

import intellect.Pursuit
import intellect.acessment.Knowledge
import intellect.acessment.character
import intellect.acessment.getVisibleEnemies
import intellect.execution.isInAttackRange
import mythic.ent.Id
import physics.voidNodeId
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
  if (world.bodyTable[knowledge.spiritId]!!.node == voidNodeId)
    return Pursuit()
  
  val targetEnemy = updateTargetEnemy(world, knowledge, pursuit)
  val target = knowledge.characters[pursuit.targetEnemy]
  val path = if (target != null) {
    val bodies = world.bodyTable
    val attackerBody = bodies[knowledge.spiritId]!!
    val ability = character(world, knowledge).abilities[0]
    if (isInAttackRange(attackerBody, target.position, ability))
      null
    else
      updateAttackMovementPath(world, knowledge, target.id, pursuit.path)
  } else
    updateRoamingPath(world, knowledge, pursuit)

  return Pursuit(
      targetEnemy = targetEnemy,
      path = path
  )
}
