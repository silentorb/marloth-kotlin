package simulation.abilities

import marloth.scenery.enums.AccessoryId
import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.accessorize.hasAccessory
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.FloatTimer
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
                accessory = Accessory(
                    type = AccessoryId.entangling,
                    owner = target,
                    level = accessory.level
                ),
                timerFloat = FloatTimer(1f)
            )
        ),
        NewHandEvent(
            hand = Hand(
                accessory = Accessory(
                    type = AccessoryId.entangleImmune,
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
          accessory.type == AccessoryId.entangling && previous.timersFloat[key]!!.duration <= 0f
        }
        .map { (_, accessory) ->
          Hand(
              accessory = Accessory(
                  type = AccessoryId.entangled,
                  owner = accessory.owner,
                  level = accessory.level
              ),
              timerFloat = FloatTimer(entangledDuration(accessory.level))
          )
        }
