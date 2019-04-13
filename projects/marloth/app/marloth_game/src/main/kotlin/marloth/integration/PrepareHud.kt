package marloth.integration

import marloth.clienting.gui.HudData
import mythic.ent.Id
import simulation.Interactable
import simulation.World

fun getVisibleInteractable(world: World, player: Id): Interactable? {
  return world.deck.interactables.filter {
    true
  }.values.firstOrNull()
}

fun gatherHudData(world: World): HudData {
  val deck = world.deck
  val player = deck.players.values.first().id
  val character = deck.characters[player]!!
  return HudData(
      health = character.health,
      sanity = character.sanity,
interactable = getVisibleInteractable(world, player)
      )
}