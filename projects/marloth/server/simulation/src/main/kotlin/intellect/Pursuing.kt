package intellect

import simulation.Commands
import mythic.ent.Id

fun updateTarget(knowledge: Knowledge, pursuit: Pursuit): Id? {
  val visibleEnemies = getVisibleEnemies(knowledge.character, knowledge)
  return if (pursuit.target != null && visibleEnemies.any { it.id == pursuit.target })
    pursuit.target
  else if (visibleEnemies.any())
    visibleEnemies.first().id
  else
    null
}

fun updatePursuit(knowledge: Knowledge, pursuit: Pursuit): Pursuit {
  val target = updateTarget(knowledge, pursuit)
  val path = if (target != null)
    pursuit.path
  else
    updateMovementPursuit(knowledge, pursuit)

  return Pursuit(
      target = target,
      path = path
  )
}

fun pursueGoal(knowledge: Knowledge, pursuit: Pursuit): Commands {
  return if (pursuit.target != null)
    spiritAttack(knowledge, pursuit)
  else
    moveSpirit(knowledge, pursuit)
}

fun pursueGoals(spirits: Collection<Spirit>): Commands {
  return spirits.flatMap {
    if (it.knowledge != null)
      pursueGoal(it.knowledge, it.pursuit)
    else
      listOf()
  }
}