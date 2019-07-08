package marloth.integration

import marloth.clienting.gui.HudData
import mythic.ent.Id
import mythic.spatial.Vector3
import simulation.misc.Interactable
import simulation.main.World
import simulation.misc.AttachmentTypeId
import simulation.misc.getTargetAttachmentsOfCategory

fun getVisibleInteractable(world: World, player: Id): Interactable? {
  val playerBody = world.deck.bodies[player]!!
  val playerCharacter = world.deck.characters[player]!!
  val f = playerCharacter.facingVector
  val facingVector = Vector3(f.x, f.y, 0f)
  return world.deck.interactables.filter { (id, _) ->
    val body = world.deck.bodies[id]!!
    body.position.distance(playerBody.position) < 4
        && (body.position - playerBody.position).normalize().dot(facingVector) > 0.8f
  }.values.firstOrNull()
}

fun gatherHudData(world: World): HudData {
  val deck = world.deck
  val player = deck.players.keys.first()
  val character = deck.characters[player]!!
  val buffs = getTargetAttachmentsOfCategory(deck, player, AttachmentTypeId.buff.name)
      .map { Pair(deck.entities[it]!!.type, deck.timers[it]!!.duration) }

  return HudData(
      health = character.health,
      sanity = character.sanity,
      interactable = getVisibleInteractable(world, player),
      buffs = buffs
  )
}
