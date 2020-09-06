package marloth.clienting.menus

import marloth.clienting.MarlothBloomState
import marloth.clienting.StateFlower
import marloth.definition.misc.staticDamageTypes
import marloth.scenery.enums.Text
import simulation.accessorize.getAccessories
import silentorb.mythic.bloom.horizontalPlane
import silentorb.mythic.bloom.label
import silentorb.mythic.bloom.list
import silentorb.mythic.bloom.next.*
import silentorb.mythic.bloom.verticalPlane
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
            localizedLabel(textStyles.smallBlack, damageTypeNames[damageType] ?: Text.unnamed)
        ),
        label(textStyles.smallBlack, value.toString() + " %")
    ))
  }
  return list(verticalPlane)(listOf(
      localizedLabel(textStyles.smallBlack, Text.gui_resistances),
      margin(20)(
          list(verticalPlane, 15)(items)
      )
  ))
}

fun generalCharacterInfo(definitions: Definitions, deck: Deck, actor: Id): Flower {
  val character = deck.characters[actor]!!
  val profession = definitions.professions[character.profession]!!
  val rows = listOf(
      localizedLabel(textStyles.smallBlack, Text.gui_profession),
      localizedLabel(textStyles.smallBlack, profession.name)
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
              localizedLabel(textStyles.smallBlack, accessoryDefinition.name)
          )
        } else
          null
      }
  return list(verticalPlane)(listOf(
      localizedLabel(textStyles.smallBlack, Text.gui_accessories),
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

fun characterInfoViewOrChooseAbilityMenu(definitions: Definitions, deck: Deck, actor: Id): StateFlower = { state ->
  val character = deck.characters[actor]!!
  val accessoryOptions = character.accessoryOptions
  if (accessoryOptions != null)
    menuFlower(definitions, Text.gui_chooseAccessoryMenu, chooseAccessoryMenu(definitions, actor, accessoryOptions))(state)
  else
    characterInfoView(definitions, deck, actor)
}
