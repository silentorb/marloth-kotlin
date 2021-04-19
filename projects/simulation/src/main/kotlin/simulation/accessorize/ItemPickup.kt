package simulation.accessorize

import marloth.scenery.enums.AccessoryIdOld
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.DeleteEntityEvent
import silentorb.mythic.happenings.Events
import silentorb.mythic.physics.Body
import silentorb.mythic.spatial.Vector3
import simulation.combat.spatial.AttackEvent
import simulation.entities.CollisionMap
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
            itemAccessory.value.type == accessory.value.type
          }
      if (available) {
        when {
          itemAccessory.value.type == AccessoryIdOld.victoryKey -> listOf(
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

fun eventsFromItemPickups(world: World): (Command) -> Events = { command ->
  val deck = world.deck
  val character = command.target as? Id
  if (character == null)
    listOf()
  else {
    val worldItem = deck.characters[character]?.canInteractWith
    val itemType = deck.interactables[worldItem]?.primaryCommand?.commandValue as? String
    if (worldItem == null || itemType == null)
      listOf()
    else {
      val stack = deck.accessories.entries
          .firstOrNull { it.value.owner == character && it.value.value.type == itemType }

      listOf(
          DeleteEntityEvent(
              id = worldItem
          ),
          if (stack != null)
            ModifyItemQuantityEvent(stack.key, 1)
          else
            NewHand(
                components = listOf(
                    AccessoryStack(
                        value = Accessory(
                            type = itemType,
                        ),
                        owner = character
                    )
                )
            )
      )
    }
  }
}
