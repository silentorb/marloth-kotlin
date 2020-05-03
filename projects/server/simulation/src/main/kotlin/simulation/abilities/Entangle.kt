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

fun entangleEvents(deck: Deck, target: Id): Events {
  val entangledDuration = 2f
  val immunityDuration = 3f
  return if (isEntangleImmune(deck.accessories, target))
    listOf()
  else
    listOf(
        NewHandEvent(
            hand = Hand(
                accessory = Accessory(
                    type = ModifierId.entangled,
                    owner = target
                ),
                timerFloat = FloatTimer(entangledDuration)
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
