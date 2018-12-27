package intellect.execution

import intellect.Pursuit
import intellect.Spirit
import intellect.acessment.Knowledge
import physics.isInVoid
import simulation.Commands
import simulation.World

fun pursueGoal(world: World, knowledge: Knowledge, pursuit: Pursuit): Commands {
  return when {
    isInVoid(world, knowledge.spiritId) -> listOf()
    pursuit.path != null -> moveSpirit(world, knowledge, pursuit.path)
    pursuit.targetPosition != null -> moveStraightTowardPosition(world, knowledge, pursuit.targetPosition)
    pursuit.targetEnemy != null -> spiritAttack(world, knowledge, pursuit)
    else -> {
      println("AI Error")
      listOf()
    }
  }
}

fun pursueGoals(world: World, spirits: Collection<Spirit>): Commands {
  return spirits.flatMap {
    if (it.knowledge != null)
      pursueGoal(world, it.knowledge, it.pursuit)
    else
      listOf()
  }
}