package marloth.clienting.menus

import silentorb.mythic.bloom.*
import silentorb.mythic.bloom.next.*
import silentorb.mythic.ent.Id
import marloth.scenery.enums.Text
import marloth.scenery.enums.staticDamageTypes
import silentorb.mythic.bloom.horizontalPlane
import silentorb.mythic.bloom.label
import silentorb.mythic.bloom.list
import silentorb.mythic.bloom.next.Flower
import silentorb.mythic.bloom.next.div
import silentorb.mythic.bloom.next.emptyFlower
import silentorb.mythic.bloom.next.margin
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
            localizedLabel(textStyles.smallBlack, damageTypeNames[damageType]!!)
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

fun characterInfoView(definitions: Definitions, deck: Deck, player: Id): Flower {
  return dialog(Text.gui_characterInfo)(
      list(horizontalPlane, 10)(listOf(
          div(forward = forwardDimensions(fixed(300), fixed(300)))(emptyFlower),
          resistancesView(deck, player)
      ))
  )
}