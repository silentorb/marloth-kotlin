package simulation.abilities

import marloth.scenery.enums.AccessoryIdOld
import simulation.accessorize.Accessory
import simulation.accessorize.hasAccessory
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.FloatTimer
import simulation.accessorize.AccessoryStack
import simulation.accessorize.hasAnyAccessory
import simulation.main.Deck
import simulation.main.Hand
import simulation.main.NewHand

val isEntangleImmune = hasAccessory(AccessoryIdOld.entangleImmune)
val isEntangled = hasAccessory(AccessoryIdOld.entangled)

fun entangledDuration(level: Int): Float =
    when (level) {
      1 -> 1.5f
      2 -> 2f
      3 -> 2.5f
      else -> 1f
    }

fun entangleEvents(deck: Deck, accessory: Accessory): (Id) -> Events = { target ->
  val immunityDuration = 3f

  if (hasAnyAccessory(setOf(AccessoryIdOld.entangling, AccessoryIdOld.entangleImmune), deck.accessories, target))
    listOf()
  else
    listOf(
        NewHand(
            components = listOf(
                AccessoryStack(
                    value = Accessory(
                        type = AccessoryIdOld.entangling,
                        level = accessory.level,
                    ),
                    owner = target,
                ),
                FloatTimer(1f)
            ),
        ),
        NewHand(
            components = listOf(
                AccessoryStack(
                    value = Accessory(
                        type = AccessoryIdOld.entangleImmune,
                    ),
                    owner = target
                ),
                FloatTimer(entangledDuration(accessory.level) + immunityDuration),
            )
        )
    )
}

fun newEntangleEntities(previous: Deck): List<Hand> =
    previous.accessories
        .filter { (key, accessory) ->
          accessory.value.type == AccessoryIdOld.entangling && previous.timersFloat[key]!!.duration <= 0f
        }
        .map { (_, accessory) ->
          Hand(
              accessory = AccessoryStack(
                  value = Accessory(
                      type = AccessoryIdOld.entangled,
                      level = accessory.value.level
                  ),
                  owner = accessory.owner,
              ),
              timerFloat = FloatTimer(entangledDuration(accessory.value.level))
          )
        }
