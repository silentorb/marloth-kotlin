package simulation.abilities

import marloth.scenery.enums.AccessoryId
import simulation.accessorize.Accessory
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.FloatTimer
import simulation.accessorize.AccessoryStack
import simulation.happenings.NewHandEvent
import simulation.main.Hand
import simulation.misc.Definitions

fun dashBonus(level: Int): Float =
    when (level) {
      1 -> 1.5f
      2 -> 2f
      3 -> 2.5f
      else -> 1f
    }

fun dashEvents(definitions: Definitions, accessory: Accessory, actor: Id): Events {
  val definition = definitions.actions[accessory.type]!!
  return listOf(
      NewHandEvent(
          hand = Hand(
              accessory = AccessoryStack(
                  value = Accessory(
                      type = AccessoryId.dashing,
                      level = accessory.level,
                  ),
                  owner = actor,
              ),
              timerFloat = FloatTimer(definition.duration)
          )
      )
  )
}
