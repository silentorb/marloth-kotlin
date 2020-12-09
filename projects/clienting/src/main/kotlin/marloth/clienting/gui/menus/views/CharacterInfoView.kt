package marloth.clienting.gui.menus.views

import marloth.clienting.StateFlower
import marloth.clienting.StateFlowerTransform
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.*
import marloth.clienting.gui.menus.general.Tab
import marloth.clienting.gui.menus.general.simpleMenuFlower
import marloth.clienting.gui.menus.general.tabView
import marloth.scenery.enums.DevText
import marloth.scenery.enums.TextId
import marloth.scenery.enums.TextResourceMapper
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

fun characterView(flower: StateFlower): StateFlowerTransform =
    dialogWrapper { definitions, state ->
      dialog(definitions, TextId.gui_characterInfo,
          boxList(verticalPlane, 10)(
              listOf(
                  tabView(definitions.textLibrary, characterViewTabs, state.view!!),
                  flower(definitions, state),
              )
          )
      )
    }

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
        val accessoryDefinition = definitions.accessories[accessoryRecord.value.type]
        if (accessoryDefinition != null && accessoryDefinition.name != TextId.unnamed) {
          label(TextStyles.smallBlack, definitions.textLibrary(accessoryDefinition.name))
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

fun characterInfoView(deck: Deck, actor: Id): StateFlowerTransform = characterView { definitions, state ->
  boxList(horizontalPlane, 10)(
      listOf(
          generalCharacterInfo(definitions, deck, actor),
          accessoriesView(definitions, deck, actor)
      )
  )
}

//fun characterInfoViewOrChooseAbilityMenu(deck: Deck, actor: Id): StateFlowerTransform =
//    dialogWrapper { definitions, state ->
//      val character = deck.characters[actor]!!
//      val accessoryOptions = character.accessoryOptions
//      if (accessoryOptions != null)
//        simpleMenuFlower(TextId.gui_chooseAccessoryMenu, chooseAccessoryMenu(definitions, actor, accessoryOptions))(definitions, state)
//      else
//        characterInfoView(definitions, deck, actor)
//    }
