package intellect

import intellect.acessment.newKnowledge
import intellect.acessment.updateKnowledge
import intellect.design.updatePursuit
import simulation.*

fun updateAiState(world: World, delta: Float): (Spirit) -> Spirit = { spirit ->
  val character = world.characterTable[spirit.id]!!
  val previousKnowledge = spirit.knowledge ?: newKnowledge(world, character)
  val knowledge = updateKnowledge(world, character, previousKnowledge, delta)
  val pursuit = updatePursuit(world, knowledge, spirit.pursuit)
  Spirit(
      spirit.id,
      knowledge = knowledge,
      pursuit = pursuit
  )
}

