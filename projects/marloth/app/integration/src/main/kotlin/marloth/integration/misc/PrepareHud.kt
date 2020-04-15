package marloth.integration.misc

import marloth.clienting.hud.HudData
import marloth.clienting.menus.ViewId
import silentorb.mythic.ent.Id
import simulation.intellect.assessment.lightRating
import simulation.main.Deck
import simulation.misc.Definitions
import simulation.misc.getActiveAction
import kotlin.math.roundToInt

fun floatToRoundedString(value: Float): String =
    ((value * 100).roundToInt().toFloat() / 100).toString()

fun gatherHudData(definitions: Definitions, deck: Deck, player: Id, view: ViewId): HudData? {
  val character = deck.characters[player]
  return if (character == null)
    null
  else {
    val destructible = deck.destructibles[player]!!
    val body = deck.bodies[player]!!

    val buffs = deck.modifiers
        .filter { it.value.target == player }
        .map { Pair(deck.modifiers[it.key]!!, deck.timersInt[it.key]!!.duration) }

    val interactable = if (view == ViewId.none)
      deck.interactables[character.canInteractWith]
    else null

    val action = getActiveAction(deck, player)
    val cooldown = if (action != null) {
      val inverse = deck.actions[action]?.cooldown!!
      val accessory = deck.accessories[action]!!
      val definition = definitions.actions[accessory.type]!!
      val c = 1f - (inverse / definition.cooldown)
      if (c == 1f)
        null
      else
        c
    } else
      null

    HudData(
        health = destructible.health,
        sanity = character.sanity,
        interactable = interactable,
        cooldown = cooldown,
        buffs = buffs,
        debugInfo = listOf(
            "LR: ${floatToRoundedString(lightRating(deck, player))}",
            floatToRoundedString(body.velocity.length())
//          if (character.isGrounded) "Grounded" else "Air",
//          character.groundDistance.toString()
        )
    )
  }
}
