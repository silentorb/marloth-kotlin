package simulation.happenings

import simulation.accessorize.eventsFromItemPickups
import simulation.combat.spatial.eventsFromMissiles
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.filterCharacterCommandsFromEvents
import silentorb.mythic.timing.emitCycleEvents
import simulation.combat.toSpatialCombatWorld
import simulation.intellect.aliveSpirits
import simulation.intellect.execution.pursueGoals
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Definitions
import simulation.misc.eventsFromVictoryKeys
import simulation.misc.toPerformanceDeck
import simulation.misc.toPerformanceDefinitions
import simulation.physics.getBulletCollisions
import simulation.updating.simulationDelta

fun eventsFromPerformances(definitions: Definitions, deck: Deck): Events =
    silentorb.mythic.performing.eventsFromPerformances(toPerformanceDefinitions(definitions), toPerformanceDeck(deck), simulationDelta)

fun getSimulationEvents(definitions: Definitions, previous: Deck, world: World, externalEvents: Events): Events {
  val deck = world.deck
  val spiritCommands = pursueGoals(world, aliveSpirits(world.deck))
  val commands = filterCharacterCommandsFromEvents(externalEvents).plus(spiritCommands)
  val collisions = getBulletCollisions(world.bulletState, deck)
      .associateBy { it.first }

//  val triggerEvents = (if (shouldUpdateLogic(deck)) {
//    val triggerings = gatherActivatedTriggers(deck, definitions, collisions, commands)
//    triggersToEvents(triggerings)
//  } else
//    listOf())

  val events = listOf(
      externalEvents,
//      triggerEvents,
      eventsFromPerformances(definitions, deck),
      commandsToEvents(commands),
      commands,
      emitCycleEvents(deck.cyclesInt),
      eventsFromMissiles(toSpatialCombatWorld(world), collisions),
      eventsFromItemPickups(world, collisions),
      eventsFromVictoryKeys(world)
  )
      .flatten()

  return events.plus(eventsFromEvents(previous, world, events))
}
