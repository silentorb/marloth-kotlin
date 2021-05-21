package simulation.happenings

import silentorb.mythic.ent.Id
import simulation.main.Deck
import simulation.main.World

fun canUse(world: World, action: Id): Boolean {
  val deck = world.deck
  val definitions = world.definitions

  val accessory = deck.accessories[action]!!
  val isWeapon = definitions.weapons.containsKey(accessory.type)
//  if (isWeapon && isAtHome(world.realm.grid, deck)(accessory.owner))
//    return false

  val actionRecord = deck.actions[action]
  return actionRecord == null || actionRecord.cooldown == 0f
}

fun canUseSimple(deck: Deck, action: Id): Boolean {
  val actionRecord = deck.actions[action]
  return actionRecord != null && actionRecord.cooldown == 0f
}
