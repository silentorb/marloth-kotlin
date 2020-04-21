package simulation.intellect.execution

import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import simulation.intellect.Pursuit
import simulation.intellect.Spirit
import simulation.intellect.assessment.Knowledge
import silentorb.mythic.happenings.Commands
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
  return spirits.flatMap { (id, spirit) ->
    val knowledge = world.deck.knowledge[id]
    val pursuit = spirit.pursuit
    if (knowledge != null && pursuit != null)
      pursueGoal(world, id, knowledge, pursuit)
    else
      listOf()
  }
}