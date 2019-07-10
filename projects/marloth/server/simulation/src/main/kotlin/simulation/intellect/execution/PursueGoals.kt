package simulation.intellect.execution

import simulation.intellect.Pursuit
import simulation.intellect.Spirit
import simulation.intellect.acessment.Knowledge
import simulation.physics.isInVoid
import simulation.input.Commands
import simulation.main.World

fun pursueGoal(world: World, knowledge: Knowledge, pursuit: Pursuit): Commands {
  return when {
    isInVoid(world, knowledge.spiritId) -> listOf()
    pursuit.path != null -> moveSpirit(world, knowledge, pursuit.path)
    pursuit.targetPosition != null -> moveStraightTowardPosition(world, knowledge, pursuit.targetPosition)
    pursuit.targetEnemy != null -> spiritAttack(world, knowledge, pursuit)
    else -> {
//      println("AI Error")
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
