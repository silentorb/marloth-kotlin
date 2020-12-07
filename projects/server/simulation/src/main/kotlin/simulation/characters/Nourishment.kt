package simulation.characters

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import simulation.accessorize.Nutrient
import simulation.happenings.TryActionEvent
import simulation.main.Deck
import simulation.misc.*
import kotlin.math.max
import kotlin.math.min

private var feetTravelled1000: Int = 0

fun getNourishmentEventsAdjustment(definitions: Definitions, deck: Deck, actor: Id, events: Events): Int =
    events
        .filterIsInstance<TryActionEvent>()
        .filter { it.actor == actor }
        .sumBy { event ->
          val components = definitions.accessories[deck.accessories[event.action]?.type]?.components ?: listOf()
          components
              .filterIsInstance<Nutrient>()
              .sumBy { it.value }
        }

fun updateNourishment(frames: Int, character: Character, adjustment: Int, velocity: Int1000): HighInt {
  val previous = if (adjustment == 0)
    character.nourishment
  else
    min(highIntScale, character.nourishment + percentageToHighInt(adjustment))

  return if (frames < 1)
    previous
  else {
    feetTravelled1000 += velocity / 60
    val delta = highIntScale * frames
    val distanceTraveledDrainDuration = 1000 // With no other factors, moving X feet will deplete full nourishment
    val timeDrainDuration = intMinute * 30 // With no other factors, waiting X frames will deplete full nourishment
    val movementCostPerFoot = highIntScale / distanceTraveledDrainDuration
    val movementCost = movementCostPerFoot * frames * velocity / 1000 / 60
    val timeCost = delta / timeDrainDuration
    val cost = movementCost + timeCost
    max(0, previous - cost)
  }
}
