package simulation.updating

import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.filterCharacterCommandsFromEvents
import silentorb.mythic.timing.emitCycleEvents
import simulation.happenings.commandsToEvents
import simulation.happenings.eventsFromEvents
import simulation.happenings.gatherActivatedTriggers
import simulation.happenings.triggersToEvents
import simulation.intellect.aliveSpirits
import simulation.intellect.execution.pursueGoals
import simulation.main.Deck
import simulation.main.World
import simulation.main.shouldUpdateLogic
import simulation.misc.Definitions
import simulation.misc.toPerformanceDeck
import simulation.misc.toPerformanceDefinitions
import simulation.physics.getBulletCollisions

fun eventsFromPerformances(definitions: Definitions, deck: Deck): Events =
    silentorb.mythic.performing.eventsFromPerformances(toPerformanceDefinitions(definitions), toPerformanceDeck(deck), simulationDelta)

fun generateIntermediateRecords(definitions: Definitions, previous: Deck, world: World, externalEvents: Events): Events {
  val deck = world.deck
  val spiritCommands = pursueGoals(world, aliveSpirits(world.deck))
  val commands = filterCharacterCommandsFromEvents(externalEvents).plus(spiritCommands)
  val collisions = getBulletCollisions(world.bulletState, deck)
  val triggerEvents = (if (shouldUpdateLogic(deck)) {
    val triggerings = gatherActivatedTriggers(deck, definitions, collisions, commands)
    triggersToEvents(triggerings)
  } else
    listOf())

  val events = externalEvents
      .plus(triggerEvents)
      .plus(eventsFromPerformances(definitions, deck))
      .plus(commandsToEvents(commands))
      .plus(commands)
      .plus(emitCycleEvents(deck.cyclesInt))

  return events.plus(eventsFromEvents(previous, world, events))
}
