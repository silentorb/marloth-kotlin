package simulation.intellect

import simulation.intellect.acessment.newKnowledge
import simulation.intellect.acessment.updateKnowledge
import simulation.intellect.design.updatePursuit
import simulation.*

fun updateAiState(world: World, delta: Float): (Spirit) -> Spirit = { spirit ->
  val character = world.characterTable[spirit.id]!!
  if (!character.isAlive)
    spirit
  else {
    val previousKnowledge = spirit.knowledge ?: newKnowledge(world, character)
    val knowledge = updateKnowledge(world, character, previousKnowledge, delta)
    val pursuit = updatePursuit(world, knowledge, spirit.pursuit)
    Spirit(
        spirit.id,
        knowledge = knowledge,
        pursuit = pursuit
    )
  }
}
