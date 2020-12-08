package marloth.clienting.gui.menus.views

import marloth.clienting.StateFlowerTransform
import marloth.clienting.gui.menus.getPlayerInteractingWith
import silentorb.mythic.bloom.emptyFlower
import silentorb.mythic.ent.Id
import simulation.main.Deck

fun emptyStateFlowerTransform(): StateFlowerTransform = { _, _ -> emptyFlower }

fun conversationView(deck: Deck, player: Id): StateFlowerTransform {
  val other = getPlayerInteractingWith(deck, player)
  val character = deck.characters[player]
  val otherCharacter = deck.characters[other]
  return if (other != null && character != null && otherCharacter != null) {
    when {
      otherCharacter.wares.any() -> merchantView(deck, player, other, character, otherCharacter)
      otherCharacter.availableContracts.any() -> contractVendorView(deck, player, other, character, otherCharacter)
      else -> emptyStateFlowerTransform()
    }
  } else
    emptyStateFlowerTransform()
}
