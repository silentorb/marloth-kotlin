package marloth.clienting.gui.menus.views

import marloth.clienting.StateFlower
import marloth.clienting.StateFlowerTransform
import marloth.clienting.gui.menus.*
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.*
import simulation.accessorize.getAccessories
import silentorb.mythic.ent.Id
import simulation.main.Deck
import simulation.misc.Definitions

//fun resistancesView(deck: Deck, player: Id): Flower {
//  val destructible = deck.destructibles[player]!!
//  val items = staticDamageTypes.map { damageType ->
//    val modifier = destructible.damageMultipliers[damageType] ?: defaultDamageMultiplier
//    val value = 100 - modifier
//
//    list(horizontalPlane, 20)(listOf(
//        div(layout = layoutDimensions(width = fixed(70)))(
//            localizedLabel(TextStyles.smallBlack, damageTypeNames[damageType] ?: Text.unnamed)
//        ),
//        label(TextStyles.smallBlack, value.toString() + " %")
//    ))
//  }
//  return list(verticalPlane)(listOf(
//      localizedLabel(TextStyles.smallBlack, Text.gui_resistances),
//      forwardMargin(20)(
//          list(verticalPlane, 15)(items)
//      )
//  ))
//}

fun generalCharacterInfo(definitions: Definitions, deck: Deck, actor: Id): Box {
  val character = deck.characters[actor]!!
  val profession = character.definition
  val rows = listOf(
      label(TextStyles.smallBlack, definitions.textLibrary(Text.gui_profession)),
      label(TextStyles.smallBlack, definitions.textLibrary(profession.name))
  )
  return boxList(verticalPlane, 10)(rows)
}

fun accessoriesView(definitions: Definitions, deck: Deck, actor: Id): Box {
  val accessories = getAccessories(deck.accessories, actor)
  val items = accessories
      .mapNotNull { (_, accessoryRecord) ->
        val accessoryDefinition = definitions.accessories[accessoryRecord.type]
//        if (accessoryDefinition != null && accessoryDefinition.name != Text.unnamed) {
//          div(layout = layoutDimensions(width = fixed(120)))(
//              localizedLabel(TextStyles.smallBlack, accessoryDefinition.name)
//          )
//        } else
        null
      }
  return boxList(verticalPlane)(listOf(
      label(TextStyles.smallBlack, definitions.textLibrary(Text.gui_accessories)),
//      reverseMargin(20)(
//          boxList(verticalPlane, 15)(flowersToBoxes(items))
//      )
  ))
}

fun characterInfoView(definitions: Definitions, deck: Deck, actor: Id): Box {
  return dialog(definitions.textLibrary(Text.gui_characterInfo))(
      boxList(horizontalPlane, 10)(
          listOf(
//          flowerToBox(div(forward = forwardDimensions(fixed(300), fixed(300)))(boxToFlower(generalCharacterInfo(definitions, deck, actor)))),
              accessoriesView(definitions, deck, actor)
          )
      )
  )
}

fun characterInfoViewOrChooseAbilityMenu(deck: Deck, actor: Id): StateFlowerTransform =
    dialogWrapperWithExtras { definitions, state ->
      val character = deck.characters[actor]!!
      val accessoryOptions = character.accessoryOptions
      if (accessoryOptions != null)
        simpleMenuFlower(Text.gui_chooseAccessoryMenu, chooseAccessoryMenu(definitions, actor, accessoryOptions))(definitions, state)
      else
        characterInfoView(definitions, deck, actor)
    }
