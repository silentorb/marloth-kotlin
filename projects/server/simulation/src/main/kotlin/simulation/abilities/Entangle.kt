package simulation.abilities

import marloth.scenery.enums.AccessoryId
import simulation.accessorize.Accessory
import simulation.accessorize.hasAccessory
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.FloatTimer
import simulation.accessorize.AccessoryStack
import simulation.happenings.NewHandEvent
import simulation.main.Deck
import simulation.main.Hand

val isEntangleImmune = hasAccessory(AccessoryId.entangleImmune)
val isEntangled = hasAccessory(AccessoryId.entangled)

fun entangledDuration(level: Int): Float =
    when (level) {
      1 -> 1.5f
      2 -> 2f
      3 -> 2.5f
      else -> 1f
    }

fun entangleEvents(deck: Deck, accessory: Accessory): (Id) -> Events = { target ->
  val immunityDuration = 3f

  if (isEntangleImmune(deck.accessories, target))
    listOf()
  else
    listOf(
        NewHandEvent(
            hand = Hand(
                accessory = AccessoryStack(
                    value = Accessory(
                        type = AccessoryId.entangling,
                        level = accessory.level,
                    ),
                    owner = target,
                ),
                timerFloat = FloatTimer(1f)
            )
        ),
        NewHandEvent(
            hand = Hand(
                accessory = AccessoryStack(
                    value = Accessory(
                        type = AccessoryId.entangleImmune,
                    ),
                    owner = target
                ),
                timerFloat = FloatTimer(entangledDuration(accessory.level) + immunityDuration)
            )
        )
    )
}

fun newEntangleEntities(previous: Deck): List<Hand> =
    previous.accessories
        .filter { (key, accessory) ->
          accessory.value.type == AccessoryId.entangling && previous.timersFloat[key]!!.duration <= 0f
        }
        .map { (_, accessory) ->
          Hand(
              accessory = AccessoryStack(
                  value = Accessory(
                      type = AccessoryId.entangled,
                      level = accessory.value.level
                  ),
                  owner = accessory.owner,
              ),
              timerFloat = FloatTimer(entangledDuration(accessory.value.level))
          )
        }
