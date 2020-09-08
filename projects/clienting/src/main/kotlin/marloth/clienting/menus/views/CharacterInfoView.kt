package marloth.clienting.menus.views

import marloth.clienting.StateFlower
import marloth.clienting.menus.*
import marloth.definition.misc.staticDamageTypes
import marloth.scenery.enums.Text
import silentorb.mythic.bloom.*
import simulation.accessorize.getAccessories
import silentorb.mythic.ent.Id
import simulation.combat.general.defaultDamageMultiplier
import simulation.main.Deck
import simulation.misc.Definitions

fun resistancesView(deck: Deck, player: Id): Flower {
  val destructible = deck.destructibles[player]!!
  val items = staticDamageTypes.map { damageType ->
    val modifier = destructible.damageMultipliers[damageType] ?: defaultDamageMultiplier
    val value = 100 - modifier

    list(horizontalPlane, 20)(listOf(
        div(layout = layoutDimensions(width = fixed(70)))(
            localizedLabel(TextStyles.smallBlack, damageTypeNames[damageType] ?: Text.unnamed)
        ),
        label(TextStyles.smallBlack, value.toString() + " %")
    ))
  }
  return list(verticalPlane)(listOf(
      localizedLabel(TextStyles.smallBlack, Text.gui_resistances),
      margin(20)(
          list(verticalPlane, 15)(items)
      )
  ))
}

fun generalCharacterInfo(definitions: Definitions, deck: Deck, actor: Id): Flower {
  val character = deck.characters[actor]!!
  val profession = definitions.professions[character.profession]!!
  val rows = listOf(
      localizedLabel(TextStyles.smallBlack, Text.gui_profession),
      localizedLabel(TextStyles.smallBlack, profession.name)
  )
  return list(verticalPlane, 10)(rows)
}

fun accessoriesView(definitions: Definitions, deck: Deck, actor: Id): Flower {
  val accessories = getAccessories(deck.accessories, actor)
  val items = accessories
      .mapNotNull { (_, accessoryRecord) ->
        val accessoryDefinition = definitions.accessories[accessoryRecord.type]
        if (accessoryDefinition != null && accessoryDefinition.name != Text.unnamed) {
          div(layout = layoutDimensions(width = fixed(120)))(
              localizedLabel(TextStyles.smallBlack, accessoryDefinition.name)
          )
        } else
          null
      }
  return list(verticalPlane)(listOf(
      localizedLabel(TextStyles.smallBlack, Text.gui_accessories),
      margin(20)(
          list(verticalPlane, 15)(items)
      )
  ))
}

fun characterInfoView(definitions: Definitions, deck: Deck, actor: Id): Flower {
  return dialog(Text.gui_characterInfo)(
      list(horizontalPlane, 10)(listOf(
          div(forward = forwardDimensions(fixed(300), fixed(300)))(generalCharacterInfo(definitions, deck, actor)),
          accessoriesView(definitions, deck, actor)
      ))
  )
}

fun characterInfoViewOrChooseAbilityMenu(deck: Deck, actor: Id): StateFlower = { definitions, state ->
  val character = deck.characters[actor]!!
  val accessoryOptions = character.accessoryOptions
  if (accessoryOptions != null)
    simpleMenuFlower(Text.gui_chooseAccessoryMenu, chooseAccessoryMenu(definitions, actor, accessoryOptions))(definitions, state)
  else
    characterInfoView(definitions, deck, actor)
}
