package simulation.accessorize

import marloth.scenery.enums.AccessoryId
import silentorb.mythic.accessorize.ChangeItemOwnerEvent
import silentorb.mythic.happenings.DeleteEntityEvent
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.Body
import silentorb.mythic.spatial.Vector3
import simulation.entities.CollisionMap
import simulation.entities.PruneEntityEvent
import simulation.main.Hand
import simulation.main.World
import simulation.misc.newVictoryKey

data class ItemPickup(
    val playerOnly: Boolean = false
)

fun eventsFromItemPickups(world: World, collisions: CollisionMap): Events {
  val deck = world.deck
  return deck.itemPickups.mapNotNull { (itemPickup, itemPickupRecord) ->
    val collision = collisions[itemPickup]
    if (collision != null && deck.characters.containsKey(collision.second) &&
        (!itemPickupRecord.playerOnly || deck.players.containsKey(collision.second))
    ) {
      val character = collision.second
      val itemAccessory = deck.accessories[itemPickup]!!
      val characterAccessories = deck.accessories.filter { it.value.owner == character }
      val available = characterAccessories
          .none { (_, accessory) ->
            itemAccessory.type == accessory.type
          }
      if (available) {
        when {
          itemAccessory.type == AccessoryId.victoryKey.name -> listOf(
              PruneEntityEvent(
                  id = itemPickup,
                  hand = newVictoryKey(owner = character)
                      .copy(
                          body = Body(
                              position = Vector3.zero
                          )
                      )
              )
          )
          else -> listOf(
              DeleteEntityEvent(
                  id = itemPickup
              )
          )
        }
            .plus(ChangeItemOwnerEvent(
                item = itemPickup,
                newOwner = character
            ))
      } else
        listOf()
    } else
      null
  }
      .flatten()
}
