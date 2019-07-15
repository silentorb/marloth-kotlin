package marloth.integration

import marloth.clienting.gui.HudData
import marloth.clienting.gui.ViewId
import simulation.main.Deck
import simulation.entities.AttachmentTypeId
import simulation.entities.getTargetAttachmentsOfCategory
import simulation.entities.getVisibleInteractable

fun gatherHudData(deck: Deck, view: ViewId): HudData {
  val player = deck.players.keys.first()
  val character = deck.characters[player]!!
  val destructible = deck.destructibles[player]!!

  val buffs = getTargetAttachmentsOfCategory(deck, player, AttachmentTypeId.buff)
      .map { Pair(deck.buffs[it]!!, deck.timers[it]!!.duration) }

  val interactable = if (view == ViewId.none)
    getVisibleInteractable(deck, player)?.value
  else null

  return HudData(
      health = destructible.health,
      sanity = character.sanity,
      interactable = interactable,
      buffs = buffs
  )
}
