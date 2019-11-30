package simulation.intellect

import mythic.ent.Id
import simulation.intellect.assessment.newKnowledge
import simulation.intellect.assessment.updateKnowledge
import simulation.intellect.design.updatePursuit
import simulation.main.World
import simulation.physics.WorldQuerySource

fun updateAiState(world: World, worldQuerySource: WorldQuerySource, delta: Float): (Id, Spirit) -> Spirit = { id, spirit ->
  val character = world.deck.characters[id]!!
  if (!character.isAlive)
    spirit
  else {
    val previousKnowledge = spirit.knowledge ?: newKnowledge(world)
    val knowledge = updateKnowledge(world, worldQuerySource, id, previousKnowledge, delta)
    val pursuit = updatePursuit(world, id, knowledge, spirit.pursuit)
    Spirit(
        knowledge = knowledge,
        pursuit = pursuit
    )
  }
}
