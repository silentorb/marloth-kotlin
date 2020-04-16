package marloth.integration.misc

import marloth.clienting.menus.ViewId
import silentorb.mythic.ent.Id
import marloth.scenery.enums.ClientCommand
import simulation.main.Deck

fun selectInteractionView(deck: Deck, id: Id): ViewId? {
  val interaction = deck.interactables[id]
  return when (interaction?.primaryCommand?.clientCommand) {
    ClientCommand.showMerchantView -> ViewId.merchant
    else -> null
  }
}
