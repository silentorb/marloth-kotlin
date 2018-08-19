package intellect

import simulation.Id
import simulation.Commands

fun newGoalId(goals: Goals): Id {
  var i = 0
  while (goals.any { it.id == i }) i++
  return i
}

fun newGoal(goals: Goals, type: GoalType): Goal =
    Goal(newGoalId(goals), type)

fun updateGoalStructure(knowledge: Knowledge, goals: Goals): Goals {
  val character = knowledge.character
  val visibleEnemy = getVisibleEnemies(character, knowledge).firstOrNull()
  return if (visibleEnemy != null)
    return listOf(newGoal(goals, GoalType.kill))
  else {
    val goals2 = goals.filter { it.type != GoalType.kill }
    if (goals2.size == 0) {
      if (knowledge.visibleCharacters.any()) {
        val k = 0
      }
      listOf(newGoal(goals2, GoalType.beAt))
    } else
      goals2
  }
}

fun updatePursuit(knowledge: Knowledge, goal: Goal, pursuit: Pursuit): Pursuit {
  return when (goal.type) {
    GoalType.beAt -> updateMovementPursuit(knowledge, pursuit)
    GoalType.kill -> pursuit
  }
}

fun pursueGoal(knowledge: Knowledge, goal: Goal, pursuit: Pursuit): Commands {
  return when (goal.type) {
    GoalType.beAt -> moveSpirit(knowledge, pursuit)
    GoalType.kill -> listOf()
  }
}

fun pursueGoals(spirits: Collection<Spirit>): Commands {
  return spirits.flatMap { pursueGoal(it.knowledge, it.goals.first(), it.pursuit) }
}

//
//fun pursueGoal(spirit: Spirit): Actions {
//  val goal = spirit.goals.firstOrNull()
//  if (goal == null)
//    throw Error("Not supported.  Maybe some day.")
//
//  return pursueGoal(spirit.knowledge, goal, spirit.pursuit)
//}