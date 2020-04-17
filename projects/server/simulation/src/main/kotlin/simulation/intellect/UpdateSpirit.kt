package simulation.intellect

import silentorb.mythic.ent.Id
import simulation.intellect.design.updatePursuit
import simulation.main.World

fun updateSpirit(world: World, delta: Float): (Id, Spirit) -> Spirit = { id, spirit ->
  val character = world.deck.characters[id]!!
  if (!character.isAlive)
    spirit
  else {
    val knowledge = world.deck.knowledge[id]!!
    val pursuit = updatePursuit(world, id, knowledge, spirit.pursuit)
    Spirit(
        pursuit = pursuit
    )
  }
}
