package marloth.clienting.gui.menus.views.interaction

import marloth.clienting.StateFlower
import marloth.clienting.StateFlowerTransform
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.Tab
import marloth.clienting.gui.menus.general.tabDialog
import marloth.scenery.enums.DevText
import silentorb.mythic.bloom.emptyFlower
import silentorb.mythic.bloom.label
import silentorb.mythic.ent.Id
import simulation.characters.Character
import simulation.entities.getContracts
import simulation.main.Deck

fun emptyStateFlowerTransform(): StateFlowerTransform = { _, _ -> emptyFlower }

typealias ConversationPageContent = (Deck, Id, Id, Character, Character) -> StateFlower

fun emptyConversationView(
    merchantCharacter: Character
): StateFlowerTransform = dialogWrapper { definitions, state ->
  dialog(definitions, merchantCharacter.definition.name,
      label(TextStyles.smallBlack, "There's nothing left.  Bye.")
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
    val activeContracts = getContracts(deck, other, player)
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

fun conversationPage(content: ConversationPageContent): (Deck, Id) -> StateFlowerTransform =
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

fun conversationView(deck: Deck, player: Id): StateFlowerTransform {
  val tabs = getConversationTabs(deck, player)
  return if (tabs.none()) {
    emptyStateFlowerTransform()
  } else
    { _, _ -> redirectFlower(tabs.first().view) }

//  val other = getPlayerInteractingWith(deck, player)
//  val character = deck.characters[player]
//  val otherCharacter = deck.characters[other]
//  return if (other != null && character != null && otherCharacter != null) {
//    val activeContracts = getContracts(deck, other, player)
//    when {
//      activeContracts.any() -> clientContractsView(deck, player, other, character, otherCharacter)
//      otherCharacter.wares.any() -> merchantView(deck, player, other, character, otherCharacter)
//      otherCharacter.availableContracts.any() -> contractVendorView(deck, player, other, character, otherCharacter)
//      else -> emptyConversationView(otherCharacter)
//    }
//  } else
//    emptyStateFlowerTransform()
}
