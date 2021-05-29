package simulation.abilities

import marloth.scenery.enums.AccessoryIdOld
import simulation.accessorize.Accessory
import simulation.accessorize.hasAccessory
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.FloatTimer
import simulation.accessorize.hasAnyAccessory
import simulation.main.Deck
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

fun entangleEvents(deck: Deck, level: Int): (Id) -> Events = { target ->
  val immunityDuration = 2.5f

  if (hasAnyAccessory(setOf(AccessoryIdOld.entangling, AccessoryIdOld.entangleImmune), deck.accessories, target))
    listOf()
  else
    listOf(
        NewHand(
            components = listOf(
                Accessory(
                    type = AccessoryIdOld.entangling,
                    level = level,
                    owner = target,
                ),
                FloatTimer(1f)
            ),
        ),
        NewHand(
            components = listOf(
                Accessory(
                    type = AccessoryIdOld.entangleImmune,
                    owner = target
                ),
                FloatTimer(entangledDuration(level) + immunityDuration),
            )
        )
    )
}

fun newEntangleEntities(previous: Deck): List<NewHand> =
    previous.accessories
        .filter { (key, accessory) ->
          accessory.type == AccessoryIdOld.entangling && previous.timersFloat[key]!!.duration <= 0f
        }
        .map { (_, accessory) ->
          NewHand(
              components = listOf(
                  Accessory(
                      type = AccessoryIdOld.entangled,
                      level = accessory.level,
                      owner = accessory.owner,
                  ),
                  FloatTimer(entangledDuration(accessory.level))
              )
          )
        }
