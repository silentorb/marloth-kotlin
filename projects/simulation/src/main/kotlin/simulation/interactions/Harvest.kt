package simulation.interactions

import silentorb.mythic.audio.NewSound
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import simulation.accessorize.Accessory
import simulation.accessorize.ModifyItemQuantityEvent
import simulation.accessorize.getAccessories
import simulation.entities.Interaction
import simulation.main.NewHand
import simulation.main.World

fun harvestEvents(world: World): (Interaction, Id) -> Events = { interaction, actor ->
  val deck = world.deck
  val container = interaction.target
  val accessory = getAccessories(deck.accessories, container).entries.firstOrNull()
  val definition = world.definitions.accessories[accessory?.value?.type]
  val sound = definition?.pickupSound
  val itemBody = deck.bodies[container]
  val quantity = accessory?.value?.quantity ?: 0

  if (quantity == 0 || accessory == null)
    listOf()
  else {
    val existingStack = deck.accessories.entries
        .firstOrNull { it.value.owner == actor && it.value.type == accessory.value.type }

    listOfNotNull(
        ModifyItemQuantityEvent(accessory.key, -quantity),
        if (sound != null && itemBody != null)
          NewSound(
              type = sound,
              position = itemBody.position,
          )
        else
          null,
        if (existingStack != null)
          ModifyItemQuantityEvent(existingStack.key, quantity)
        else
          NewHand(
              components = listOf(
                  Accessory(
                      type = accessory.value.type,
                      owner = actor,
                      quantity = quantity,
                  ),
              )
          )
    )
  }
}
