package marloth.clienting.gui.menus.views

import marloth.clienting.StateFlowerTransform
import marloth.clienting.gui.menus.TextStyles
import marloth.clienting.gui.menus.dialog
import marloth.clienting.gui.menus.dialogWrapper
import marloth.clienting.gui.menus.general.menuFlower
import marloth.clienting.gui.menus.getPlayerInteractingWith
import silentorb.mythic.bloom.boxList2
import silentorb.mythic.bloom.emptyFlower
import silentorb.mythic.bloom.horizontalPlane
import silentorb.mythic.bloom.label
import silentorb.mythic.ent.Id
import simulation.characters.Character
import simulation.main.Deck

fun emptyStateFlowerTransform(): StateFlowerTransform = { _, _ -> emptyFlower }

fun emptyConversationView(
    merchantCharacter: Character
): StateFlowerTransform = dialogWrapper { definitions, state ->
  dialog(definitions, merchantCharacter.definition.name,
      label(TextStyles.smallBlack, "There's nothing left.  Bye.")
  )
}

fun conversationView(deck: Deck, player: Id): StateFlowerTransform {
  val other = getPlayerInteractingWith(deck, player)
  val character = deck.characters[player]
  val otherCharacter = deck.characters[other]
  return if (other != null && character != null && otherCharacter != null) {
    when {
      otherCharacter.wares.any() -> merchantView(deck, player, other, character, otherCharacter)
      otherCharacter.availableContracts.any() -> contractVendorView(deck, player, other, character, otherCharacter)
      else -> emptyConversationView(otherCharacter)
    }
  } else
    emptyStateFlowerTransform()
}
