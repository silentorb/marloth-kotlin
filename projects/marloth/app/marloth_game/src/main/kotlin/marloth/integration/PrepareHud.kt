package marloth.integration

import marloth.clienting.gui.HudData
import marloth.clienting.gui.ViewId
import silentorb.mythic.ent.Id
import simulation.main.Deck
import simulation.entities.AttachmentCategory
import simulation.entities.getTargetAttachmentsOfCategory
import simulation.intellect.assessment.lightRating
import kotlin.math.roundToInt

fun floatToRoundedString(value: Float): String =
    ((value * 100).roundToInt().toFloat() / 100).toString()

fun gatherHudData(deck: Deck, player: Id, view: ViewId): HudData? {
  val character = deck.characters[player]
  if (character == null)
    return null

  val destructible = deck.destructibles[player]!!
  val body = deck.bodies[player]!!

  val buffs = getTargetAttachmentsOfCategory(deck, player, AttachmentCategory.buff)
      .map { Pair(deck.buffs[it]!!, deck.timers[it]!!.duration) }

  val interactable = if (view == ViewId.none)
    deck.interactables[character.canInteractWith]
  else null

  return HudData(
      health = destructible.health,
      sanity = character.sanity,
      interactable = interactable,
      buffs = buffs,
      debugInfo = listOf(
          "LR: ${floatToRoundedString(lightRating(deck, player))}",
          floatToRoundedString(body.velocity.length())
//          if (character.isGrounded) "Grounded" else "Air",
//          character.groundDistance.toString()
      )
  )
}
