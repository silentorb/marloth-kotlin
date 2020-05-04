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

fun defaultDashBonus(): Float = 2f

fun dashEvents(definitions: Definitions, accessoryType: AccessoryName, actor: Id): Events {
  val definition = definitions.actions[accessoryType]!!
  return listOf(
      NewHandEvent(
          hand = Hand(
              accessory = Accessory(
                  type = AccessoryId.dashing,
                  owner = actor
              ),
              timerFloat = FloatTimer(definition.duration)
          )
      )
  )
}
