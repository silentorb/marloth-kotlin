package intellect

import simulation.*

data class SpiritUpdateResult(
    val state: Spirit,
    val actions: List<Action> = listOf()
)

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

data class CharacterResult(
    val actions: List<Action> = listOf(),
    val newMissile: NewMissile? = null
)
