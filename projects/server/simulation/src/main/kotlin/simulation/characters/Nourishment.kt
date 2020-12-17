package simulation.characters

import silentorb.mythic.ent.Id
import silentorb.mythic.happenings.Events
import simulation.accessorize.Nutrient
import simulation.happenings.TryActionEvent
import simulation.main.Deck
import simulation.misc.*
import kotlin.math.max
import kotlin.math.min

val nourishmentRates = ResourceRates(
    distanceTraveledDrainDuration = 1000,
    timeDrainDuration = intMinute * 30,
)

fun getNourishmentEventsAdjustment(definitions: Definitions, deck: Deck, actor: Id, events: Events): Int =
    events
        .filterIsInstance<TryActionEvent>()
        .filter { it.actor == actor }
        .sumBy { event ->
          val components = definitions.accessories[deck.accessories[event.action]?.value?.type]?.components ?: listOf()
          components
              .filterIsInstance<Nutrient>()
              .sumBy { it.value }
        }

val updateNourishment = updatePercentageResource(nourishmentRates)
