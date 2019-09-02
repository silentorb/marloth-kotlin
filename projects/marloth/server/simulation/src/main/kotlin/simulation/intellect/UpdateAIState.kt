package simulation.intellect

import mythic.ent.Id
import simulation.intellect.acessment.newKnowledge
import simulation.intellect.acessment.updateKnowledge
import simulation.intellect.design.updatePursuit
import simulation.main.World

fun updateAiState(world: World, delta: Float): (Id, Spirit) -> Spirit = { id, spirit ->
  val character = world.deck.characters[id]!!
  if (!character.isAlive)
    spirit
  else {
    val previousKnowledge = spirit.knowledge ?: newKnowledge(world, id)
    val knowledge = updateKnowledge(world, id, previousKnowledge, delta)
    val pursuit = updatePursuit(world, knowledge, spirit.pursuit)
    Spirit(
        knowledge = knowledge,
        pursuit = pursuit
    )
  }
}
