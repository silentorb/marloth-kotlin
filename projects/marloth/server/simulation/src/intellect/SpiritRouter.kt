package intellect

import simulation.*

fun updateAiState(world: World, spirit: Spirit): Spirit {
  val character = world.characterTable[spirit.id]!!
  val knowledge = updateKnowledge(world, character, spirit.knowledge)
//  val goals = updateGoalStructure(knowledge, )
  val pursuit = updatePursuit(knowledge, spirit.pursuit)
  return Spirit(
      spirit.id,
      knowledge = knowledge,
      pursuit = pursuit
  )
}

