package intellect

import simulation.*

//enum class SpiritActionType {
//  beAt
//}
//
//data class SpiritAction(
//    val type: SpiritActionType,
//    val offset: Vector3
//)

data class SpiritUpdateResult(
    val state: Spirit,
    val actions: List<Action> = listOf()
)

fun getAiCharacters(world: World) =
    world.characters.filter { isPlayer(world, it) }

fun updateAiState(world: World, spirit: Spirit): Spirit {
  val character = world.characterTable[spirit.id]!!
  val knowledge = updateKnowledge(world, character, spirit.knowledge)
  val goals = updateGoalStructure(knowledge, spirit.goals)
  val pursuit = updatePursuit(knowledge, goals.first(), spirit.pursuit)
  return Spirit(
      spirit.id,
      knowledge = knowledge,
      goals = goals,
      pursuit = pursuit
  )
//  val goal = getCurrentGoal(world, spirit, goals)
//  if (goal == null)
//    throw Error("Not supported.  Maybe some day.")
//
//  val actions = pursueGoal(knowledge, goal)
//  return  SpiritUpdateResult()
//  return when (spirit.state.mode) {
////    GoalType.idle -> SpiritUpdateResult(startRoaming(world, spirit))
//    GoalType.moving -> moveSpirit(spirit)
//    GoalType.kill -> updateAttack(world, spirit)
//  }
}

//fun applySpiritAction(spirit: Spirit, action: SpiritAction, delta: Float): Force? {
//  return when (action.type) {
//    SpiritActionType.beAt -> movementForce(spirit, action, delta)
//  }
//}

data class CharacterResult(
    val actions: List<Action> = listOf(),
    val newMissile: NewMissile? = null
)

//fun updateSpirit(world: World, spirit: Spirit, delta: Float): Actions {
//  spirit.state = updateAiState(world, spirit)
////  val forces = result.actions.mapNotNull { applySpiritAction(spirit, it, delta) }
//  return pursueGoal(spirit)
////  return tryAiAttack(spirit)
//}