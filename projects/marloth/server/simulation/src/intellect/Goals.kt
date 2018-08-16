package intellect

import simulation.Actions
import simulation.Id

fun newGoalId(goals: Goals): Id {
  var i = 0
  while (goals.any {it.id == i}) i++
  return i
}

fun newGoal(goals: Goals, type: GoalType): Goal =
    Goal(newGoalId(goals), type)

fun updateGoalStructure(knowledge: Knowledge, goals: Goals): Goals {
  val character = knowledge.character
  if (goals.size == 0) {
    if (knowledge.visibleCharacters.any()) {
      val k = 0
    }
    val visibleEnemy = getVisibleEnemies(character, knowledge).firstOrNull()
    return if (visibleEnemy != null)
    //    return SpiritUpdateResult(enemySightingState)
      listOf()
    else
      listOf(newGoal(goals, GoalType.beAt))
  }
  return goals
}

fun updatePursuit(knowledge: Knowledge, goal: Goal, pursuit: Pursuit): Pursuit {
  return when (goal.type) {
    GoalType.beAt -> updateMovementPursuit(knowledge, pursuit)
//    GoalType.faceTarget->
    GoalType.kill -> pursuit
  }
}

fun pursueGoal(knowledge: Knowledge, goal: Goal, pursuit: Pursuit): Actions {
  return when (goal.type) {
    GoalType.beAt -> moveSpirit(knowledge, pursuit)
//    GoalType.faceTarget->
    GoalType.kill -> listOf()
  }
}

fun pursueGoal(spirit: Spirit): Actions {
  val goal = spirit.goals.firstOrNull()
  if (goal == null)
    throw Error("Not supported.  Maybe some day.")

  return pursueGoal(spirit.knowledge, goal, spirit.pursuit)
}