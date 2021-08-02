package simulation.interactions

import marloth.scenery.enums.Textures
import silentorb.mythic.audio.NewSound
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.lookinglass.Material
import simulation.accessorize.ModifyItemQuantityEvent
import simulation.entities.Interaction
import simulation.main.Deck
import simulation.main.NewHand
import simulation.main.World
import simulation.updating.replaceDepiction

fun changeBushTexture(deck: Deck, bush: Id, newTexture: String): GameEvent? {
  val child = deck.bodies.entries.firstOrNull { it.value.parent == bush }?.key
  val depiction = deck.depictions[child]
  return if (depiction != null) {
    val material = depiction.material
    Command(
        type = replaceDepiction,
        target = child,
        value = depiction.copy(
            material = material?.copy(
                texture = newTexture,
            )
                ?: Material(
                    texture = newTexture,
                    shading = true,
                )
        )
    )
  } else
    null
}

fun harvestEvents(world: World): (Interaction, Id) -> Events = { interaction, actor ->
  val deck = world.deck
  val worldItem = interaction.target
  val stack = deck.accessories[worldItem]
  val definition = world.definitions.accessories[stack?.type]
  val sound = definition?.pickupSound
  val itemBody = deck.bodies[worldItem]

  if (stack?.quantity == 0)
    listOf()
  else
    listOfNotNull(
//        changeBushTexture(deck, worldItem, Textures.leafFloor),
        ModifyItemQuantityEvent(worldItem, -1),
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
