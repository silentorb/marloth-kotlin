package intellect.execution

import intellect.Pursuit
import intellect.Spirit
import intellect.acessment.Knowledge
import intellect.acessment.convertKnowledge
import simulation.Commands
import simulation.World

fun pursueGoal(knowledge: Knowledge, pursuit: Pursuit): Commands {
  return when {
    pursuit.path != null -> moveSpirit(knowledge, pursuit.path)
    pursuit.targetEnemy != null -> spiritAttack(knowledge, pursuit)
    else -> throw Error("Not supported")
  }
}

fun pursueGoals(world: World, spirits: Collection<Spirit>): Commands {
  return spirits.flatMap {
    if (it.knowledge != null)
      pursueGoal(convertKnowledge(world, it.knowledge), it.pursuit)
    else
      listOf()
  }
}