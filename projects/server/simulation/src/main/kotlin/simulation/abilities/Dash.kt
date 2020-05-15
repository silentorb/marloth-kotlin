package simulation.abilities

import marloth.scenery.enums.AccessoryId
import silentorb.mythic.accessorize.Accessory
import silentorb.mythic.accessorize.AccessoryName
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.FloatTimer
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
              accessory = Accessory(
                  type = AccessoryId.dashing,
                  owner = actor,
                  level = accessory.level
              ),
              timerFloat = FloatTimer(definition.duration)
          )
      )
  )
}
