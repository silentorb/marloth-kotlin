package simulation.happenings

import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.filterCharacterCommandsFromEvents
import silentorb.mythic.timing.emitCycleEvents
import simulation.accessorize.eventsFromItemPickups
import simulation.combat.spatial.eventsFromMissiles
import simulation.combat.toSpatialCombatWorld
import simulation.entities.eventsFromRespawnCountdowns
import simulation.intellect.aliveSpirits
import simulation.intellect.execution.pursueGoals
import simulation.main.Deck
import simulation.main.World
import simulation.misc.*
import simulation.movement.getFreedomTable
import simulation.movement.mobilityEvents
import simulation.physics.getBulletCollisions
import simulation.updating.simulationDelta

fun eventsFromPerformances(definitions: Definitions, deck: Deck): Events =
    silentorb.mythic.performing.eventsFromPerformances(toPerformanceDefinitions(definitions), toPerformanceDeck(deck), simulationDelta)

fun withSimulationEvents(definitions: Definitions, previous: Deck, world: World, externalEvents: Events): Events {
  val deck = world.deck
  val spiritEvents = pursueGoals(world, aliveSpirits(world.deck))
  val spiritCommands= spiritEvents.filterIsInstance<CharacterCommand>()

  val commands = filterCharacterCommandsFromEvents(externalEvents).plus(spiritCommands)
  val collisions = getBulletCollisions(world.bulletState, deck)
      .associateBy { it.first }

  val freedomTable = getFreedomTable(deck)

  val events = listOf(
      externalEvents,
      eventsFromPerformances(definitions, deck),
      commandsToEvents(deck, commands),
      emitCycleEvents(deck.cyclesInt),
      eventsFromMissiles(toSpatialCombatWorld(world), collisions),
      eventsFromItemPickups(world, collisions),
      eventsFromVictoryKeys(world),
      eventsFromRespawnCountdowns(previous, world.deck),
      if (getDebugBoolean("ENABLE_MOBILITY")) mobilityEvents(world.definitions, world.deck, commands) else listOf()
  )
      .flatten() + spiritEvents

  return events.plus(eventsFromEvents(world, freedomTable, events))
}
