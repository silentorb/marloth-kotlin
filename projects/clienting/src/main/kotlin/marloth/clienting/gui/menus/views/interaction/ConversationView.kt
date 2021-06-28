package marloth.clienting.gui.menus.views.interaction

import marloth.clienting.gui.StateBox
import marloth.clienting.gui.StateFlower
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.Tab
import marloth.clienting.gui.menus.general.tabDialog
import marloth.scenery.enums.DevText
import silentorb.mythic.bloom.label
import silentorb.mythic.ent.Id
import simulation.characters.Character
import simulation.entities.ContractStatus
import simulation.entities.getContracts
import simulation.main.Deck

fun emptyStateFlowerTransform(): StateFlower = { _, _ -> redirectFlower(null) }

typealias ConversationPageContent = (Deck, Id, Id, Character, Character) -> StateBox

fun emptyConversationView(
    merchantCharacter: Character
): StateFlower = dialogWrapper { definitions, state ->
  dialog(definitions, merchantCharacter.definition.name,
      label(TextStyles.smallBlack, "There's nothing left to say other than...bye.")
  )
}

fun conversationData(deck: Deck, player: Id): Triple<Id?, Character?, Character?> {
  val other = getPlayerInteractingWith(deck, player)
  return Triple(
      other,
      deck.characters[player],
      deck.characters[other]
  )
}

fun getConversationTabs(deck: Deck, player: Id): List<Tab> {
  val (other, character, otherCharacter) = conversationData(deck, player)
  return if (other != null && character != null && otherCharacter != null) {
    val activeContracts = getContracts(deck, other, player, status = ContractStatus.active)
    listOfNotNull(
        if (activeContracts.any())
          Tab(ViewId.conversationActiveContracts, DevText("Active Contracts"))
        else null,

        if (otherCharacter.availableContracts.any())
          Tab(ViewId.conversationAvailableContracts, DevText("Available Contracts"))
        else null,

        if (otherCharacter.wares.any())
          Tab(ViewId.conversationMerchandise, DevText("Merchandise"))
        else null,
    )
  } else
    listOf()
}

fun conversationPage(content: ConversationPageContent): (Deck, Id) -> StateFlower =
    { deck, player ->
      val other = getPlayerInteractingWith(deck, player)
      val character = deck.characters[player]
      val otherCharacter = deck.characters[other]
      if (other != null && character != null && otherCharacter != null) {
        val tabs = getConversationTabs(deck, player)
        val contentFlower = content(deck, player, other, character, otherCharacter)
        tabDialog(otherCharacter.definition.name, tabs)(contentFlower)
      } else
        emptyStateFlowerTransform()
    }

fun conversationView(deck: Deck, player: Id): StateFlower {
  val tabs = getConversationTabs(deck, player)
  return if (tabs.none()) {
    val other = getPlayerInteractingWith(deck, player)
    val otherCharacter = deck.characters[other]
    if (otherCharacter != null)
      emptyConversationView(otherCharacter)
    else
      emptyStateFlowerTransform()
  } else
    { _, _ -> redirectFlower(tabs.first().view) }
}
