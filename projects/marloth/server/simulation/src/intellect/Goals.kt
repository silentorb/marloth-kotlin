package intellect

import simulation.Id

//fun newGoalId(goals: Goals): Id {
//  var i = 0
//  while (goals.any { it.id == i }) i++
//  return i
//}

//fun newGoal(goals: Goals, type: GoalType): Goal =
//    Goal(newGoalId(goals), type)

//fun updateGoalStructure(knowledge: Knowledge, pursuit: Pursuit): Goals {
//  val character = knowledge.character
//  val visibleEnemy = getVisibleEnemies(character, knowledge).firstOrNull()
//  return if (visibleEnemy != null)
//    return listOf(newGoal(goals, GoalType.kill))
//  else {
//    val goals2 = goals.filter { it.type != GoalType.kill }
//    if (goals2.size == 0) {
//      if (knowledge.visibleCharacters.any()) {
//        val k = 0
//      }
//      listOf(newGoal(goals2, GoalType.beAt))
//    } else
//      goals2
//  }
//}
