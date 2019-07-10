package marloth.integration

import marloth.clienting.gui.HudData
import marloth.clienting.gui.ViewId
import mythic.ent.Id
import mythic.spatial.Vector3
import simulation.main.Deck
import simulation.misc.Interactable
import simulation.main.World
import simulation.misc.AttachmentTypeId
import simulation.misc.getTargetAttachmentsOfCategory
import simulation.misc.getVisibleInteractable

fun gatherHudData(deck: Deck, view: ViewId): HudData {
  val player = deck.players.keys.first()
  val character = deck.characters[player]!!
  val buffs = getTargetAttachmentsOfCategory(deck, player, AttachmentTypeId.buff)
      .map { Pair(deck.entities[it]!!.type, deck.timers[it]!!.duration) }

  val interactable = if (view == ViewId.none)
    getVisibleInteractable(deck, player)?.value
  else null

  return HudData(
      health = character.health,
      sanity = character.sanity,
      interactable = interactable,
      buffs = buffs
  )
}
