package simulation.intellect.execution

import mythic.ent.Id
import mythic.ent.Table
import simulation.intellect.Pursuit
import simulation.intellect.Spirit
import simulation.intellect.assessment.Knowledge
import simulation.input.Commands
import simulation.main.World

fun pursueGoal(world: World, character: Id, knowledge: Knowledge, pursuit: Pursuit): Commands {
  return when {

    pursuit.targetEnemy != null && isTargetInRange(world, character, pursuit.targetEnemy) ->
      spiritAttack(world, character, knowledge, pursuit)

    pursuit.targetPosition != null -> moveSpirit(world, character, knowledge, pursuit)

//    pursuit.targetPosition != null -> moveStraightTowardPosition(world, knowledge, pursuit.targetPosition)

    else -> {
//      println("AI Error")
      listOf()
    }
  }
}

fun pursueGoals(world: World, spirits: Table<Spirit>): Commands {
  return spirits.flatMap {
    val knowledge = it.value.knowledge
    if (knowledge != null)
      pursueGoal(world, it.key, knowledge, it.value.pursuit)
    else
      listOf()
  }
}
