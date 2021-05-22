package simulation.abilities

import marloth.scenery.enums.AccessoryIdOld
import simulation.accessorize.Accessory
import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import silentorb.mythic.timing.FloatTimer
import simulation.main.NewHand
import simulation.misc.Definitions

fun dashBonus(level: Int): Float =
    2f
//    when (level) {
//      1 -> 1.5f
//      2 -> 2f
//      3 -> 2.5f
//      else -> 1f
//    }

fun dashEvents(definitions: Definitions, accessory: Accessory, actor: Id): Events {
  val definition = definitions.actions[accessory.type]!!
  return listOf(
      NewHand(
          components = listOf(
              Accessory(
                  type = AccessoryIdOld.dashing,
                  level = definitions.accessories[accessory.type]!!.level,
                  owner = actor,
              ),
              FloatTimer(definition.duration),
          )
      )
  )
}
