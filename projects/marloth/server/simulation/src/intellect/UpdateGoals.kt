package intellect

import simulation.Actions
import simulation.Character
import simulation.World

fun getCurrentGoal(spirit: Spirit, goal: Goal): Goal {
  return if (goal.dependencies.none())
    goal
  else
    getCurrentGoal(spirit, goal.dependencies.first())
}

fun getCurrentGoal(spirit: Spirit): Goal? =
    if (spirit.state.goals.any())
      getCurrentGoal(spirit, spirit.state.goals.first())
    else
      null

fun updateGoalStructure(world: World, character: Character, knowledge: Knowledge, goals: Goals): Goals {

  if (goals.size == 0) {
    val visibleEnemy = getVisibleEnemies(character, knowledge).firstOrNull()
    return if (visibleEnemy != null)
    //    return SpiritUpdateResult(enemySightingState)
      listOf()
      else
    listOf(startRoaming(world, character))
  }
  return goals
}

fun pursueGoal(knowledge: Knowledge, goal: Goal): Actions {
  return when(goal.type){
    GoalType.beAt->
  }
}

fun pursueGoal(spirit: Spirit): Actions {
  val goal = getCurrentGoal(spirit)
  if (goal == null)
    throw Error("Not supported.  Maybe some day.")

  return pursueGoal(spirit.state.knowledge, goal)
}