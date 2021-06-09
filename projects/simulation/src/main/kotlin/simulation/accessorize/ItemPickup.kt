package simulation.accessorize

import marloth.scenery.enums.AccessoryIdOld
import silentorb.mythic.audio.NewSound
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.DeleteEntityEvent
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.Body
import silentorb.mythic.spatial.Vector3
import simulation.entities.CollisionMap
import simulation.entities.Interaction
import simulation.entities.PruneEntityEvent
import simulation.main.NewHand
import simulation.main.World
import simulation.misc.newVictoryKey

data class ItemPickup(
    val playerOnly: Boolean = false
)

fun eventsFromCollisionItemPickups(world: World, collisions: CollisionMap): Events {
  val deck = world.deck
  return deck.itemPickups.mapNotNull { (itemPickup, itemPickupRecord) ->
    val collision = collisions[itemPickup]
    if (collision != null && deck.characters.containsKey(collision.second) &&
        (!itemPickupRecord.playerOnly || deck.players.containsKey(collision.second))
    ) {
      val character = collision.second as Id
      val itemAccessory = deck.accessories[itemPickup]!!
      val characterAccessories = deck.accessories.filter { it.value.owner == character }
      val available = characterAccessories
          .none { (_, accessory) ->
            itemAccessory.type == accessory.type
          }
      if (available) {
        when (itemAccessory.type) {
          AccessoryIdOld.victoryKey -> listOf(
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

fun eventsFromItemPickup(world: World): (Interaction, Id) -> Events = { interaction, actor ->
  val deck = world.deck
  val worldItem = interaction.target
  val stack = deck.accessories[worldItem]
  val definition = world.definitions.accessories[stack?.type]
  val sound = definition?.pickupSound
  val itemBody = deck.bodies[worldItem]

  listOfNotNull(
      DeleteEntityEvent(
          id = worldItem
      ),
      if (sound != null && itemBody != null)
        NewSound(
            type = sound,
            position = itemBody.position,
        )
      else
        null,
      if (stack == null)
        null
      else {
        val existingStack = deck.accessories.entries
            .firstOrNull { it.value.owner == actor && it.value.type == stack.type }

        if (existingStack != null)
          ModifyItemQuantityEvent(existingStack.key, 1)
        else
          NewHand(
              components = listOf(
                  stack.copy(
                      owner = actor
                  )
              )
          )
      }
  )
}
