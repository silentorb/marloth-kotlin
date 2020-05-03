package simulation.abilities

import marloth.scenery.enums.ModifierId
import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.accessorize.hasAccessory
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.FloatTimer
import simulation.happenings.NewHandEvent
import simulation.main.Deck
import simulation.main.Hand

val isEntangleImmune = hasAccessory(ModifierId.entangleImmune)
val isEntangled = hasAccessory(ModifierId.entangled)

private const val entangledDuration = 2f

fun entangleEvents(deck: Deck): (Id) -> Events = { target ->
  val immunityDuration = 3f

  if (isEntangleImmune(deck.accessories, target))
    listOf()
  else
    listOf(
        NewHandEvent(
            hand = Hand(
                accessory = Accessory(
                    type = ModifierId.entangling,
                    owner = target
                ),
                timerFloat = FloatTimer(1f)
            )
        ),
        NewHandEvent(
            hand = Hand(
                accessory = Accessory(
                    type = ModifierId.entangleImmune,
                    owner = target
                ),
                timerFloat = FloatTimer(entangledDuration + immunityDuration)
            )
        )
    )
}

fun newEntangleEntities(previous: Deck): List<Hand> =
    previous.accessories
        .filter { (key, accessory) ->
          accessory.type == ModifierId.entangling && previous.timersFloat[key]!!.duration <= 0f
        }
        .map { (_, accessory) ->
          Hand(
              accessory = Accessory(
                  type = ModifierId.entangled,
                  owner = accessory.owner
              ),
              timerFloat = FloatTimer(entangledDuration)
          )
        }
