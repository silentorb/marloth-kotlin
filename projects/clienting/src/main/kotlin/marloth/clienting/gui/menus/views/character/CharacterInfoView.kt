package marloth.clienting.gui.menus.views.character

import marloth.clienting.gui.StateFlowerTransform
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.Tab
import marloth.clienting.gui.menus.general.tabDialog
import marloth.clienting.gui.menus.general.verticalList
import marloth.clienting.gui.menus.views.interaction.moneyLabel
import marloth.scenery.enums.DevText
import marloth.scenery.enums.TextId
import silentorb.mythic.bloom.*
import simulation.accessorize.getAccessories
import silentorb.mythic.ent.Id
import simulation.main.Deck
import simulation.misc.Definitions

val characterViewTabs = listOf(
    Tab(ViewId.characterInventory, DevText("Inventory")),
    Tab(ViewId.characterStatus, DevText("Status")),
    Tab(ViewId.characterContracts, DevText("Contracts")),
)

val characterView = tabDialog(TextId.gui_characterInfo, characterViewTabs)

fun generalCharacterInfo(definitions: Definitions, deck: Deck, actor: Id): Box {
  val character = deck.characters[actor]!!
  val profession = character.definition
  val rows = listOf(
//      label(TextStyles.smallBoldBlack, definitions.textLibrary(TextId.gui_profession)),
//      label(TextStyles.smallBlack, definitions.textLibrary(profession.name)),
//      label(TextStyles.smallBoldBlack, definitions.textLibrary(TextId.gui_money)),
      moneyLabel(definitions.textLibrary, character.money),
  )
  return boxList(verticalPlane, 10)(rows)
}

fun accessoriesView(definitions: Definitions, deck: Deck, actor: Id): Box {
  val accessories = getAccessories(deck.accessories, actor)
  val items = accessories
      .mapNotNull { (_, accessoryRecord) ->
        val accessoryDefinition = definitions.accessories[accessoryRecord.type]
        if (accessoryDefinition != null && accessoryDefinition.name != TextId.unnamed) {
          label(TextStyles.smallBlack, actionItemText(definitions, accessoryDefinition, accessoryRecord.quantity))
        } else
          null
      }
  return boxList(verticalPlane)(listOf(
      label(TextStyles.smallSemiBoldBlack, definitions.textLibrary(TextId.gui_accessories)),
      boxMargin(20)(
          boxList(verticalPlane, 15)(items)
      )
  ))
}

fun characterContractsView(deck: Deck, actor: Id): StateFlowerTransform = characterView { definitions, state ->
  val contracts = deck.contracts.filter { it.value.agent == actor }
  if (contracts.any())
    verticalList(
        contracts.map { (id, contract) ->
          label(TextStyles.smallBlack, definitions.textLibrary(contract.definition.name))
        }
    )
  else
    label(TextStyles.smallBlack, "You have no contracts.  Get a job.")
}

fun characterInventoryView(deck: Deck, actor: Id): StateFlowerTransform = characterView { definitions, state ->
  accessoriesView(definitions, deck, actor)
}

fun characterInfoView(deck: Deck, actor: Id): StateFlowerTransform = characterView { definitions, state ->
  generalCharacterInfo(definitions, deck, actor)
}
