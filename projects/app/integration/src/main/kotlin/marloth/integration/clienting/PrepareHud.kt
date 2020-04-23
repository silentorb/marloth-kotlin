package marloth.integration.clienting

import marloth.clienting.hud.HudData
import marloth.clienting.menus.ViewId
import silentorb.mythic.characters.ViewMode
import silentorb.mythic.ent.Id
import simulation.happenings.getActiveAction
import simulation.intellect.assessment.lightRating
import simulation.main.World
import simulation.misc.getPointCell
import simulation.misc.getVictoryKeyStats
import kotlin.math.roundToInt

fun floatToRoundedString(value: Float): String =
    ((value * 100).roundToInt().toFloat() / 100).toString()

fun gatherHudData(world: World, player: Id, view: ViewId): HudData? {
  val deck = world.deck
  val definitions = world.definitions
  val grid = world.realm.grid

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

    val victoryKeyStats = getVictoryKeyStats(grid, deck)

    val cell = getPointCell(body.position)
    HudData(
        health = destructible.health,
        sanity = character.sanity,
        interactable = interactable,
        cooldown = cooldown,
        buffs = buffs,
        viewMode = deck.characterRigs[player]?.viewMode,
        debugInfo = listOf(
//            "LR: ${floatToRoundedString(lightRating(deck, player))}",
//            floatToRoundedString(body.velocity.length()),
            "Keys: ${victoryKeyStats.collected}/${victoryKeyStats.total}"
//            "${cell.x} ${cell.y} ${cell.z}",
//            "${body.position.x.roundToInt()} ${body.position.y.roundToInt()} ${body.position.z.roundToInt()}"
//          if (character.isGrounded) "Grounded" else "Air",
//          character.groundDistance.toString()
        )
    )
  }
}
