package intellect

import simulation.*
import simulation.combat.NewMissile

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
}

