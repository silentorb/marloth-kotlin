package simulation.happenings

import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Commands
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.filterCharacterCommandsFromEvents
import simulation.accessorize.eventsFromItemPickups
import simulation.combat.spatial.eventsFromMissiles
import simulation.combat.toSpatialCombatWorld
import simulation.entities.eventsFromRespawnCountdowns
import simulation.entities.gatherInteractCommands
import simulation.intellect.aliveSpirits
import simulation.intellect.execution.pursueGoals
import simulation.intellect.execution.spiritsHandleRequests
import simulation.main.Deck
import simulation.main.World
import simulation.misc.*
import simulation.movement.getFreedomTable
import simulation.movement.mobilityEvents
import simulation.physics.getBulletCollisions
import simulation.updating.simulationDelta

fun eventsFromPerformances(definitions: Definitions, deck: Deck): Events =
    silentorb.mythic.performing.eventsFromPerformances(toPerformanceDefinitions(definitions), toPerformanceDeck(deck), simulationDelta)

fun withSimulationEvents(definitions: Definitions, previousDeck: Deck, world: World, externalEvents: Events): Events {
  val deck = world.deck
  val freedomTable = getFreedomTable(deck)
  val spirits = aliveSpirits(world.deck)
  val spiritPursuitEvents = pursueGoals(world, spirits, freedomTable)
  val spiritCommands = spiritPursuitEvents.filterIsInstance<Command>()

  val commands = filterCharacterCommandsFromEvents(externalEvents).plus(spiritCommands)
  val collisions = getBulletCollisions(world.bulletState, deck)
      .associateBy { it.first }

  val events = listOf(
      externalEvents,
      eventsFromPerformances(definitions, deck),
      commandsToEvents(definitions, deck, commands),
      eventsFromMissiles(toSpatialCombatWorld(world), collisions),
      eventsFromItemPickups(world, collisions),
      eventsFromVictoryKeys(world),
      eventsFromRespawnCountdowns(previousDeck, world.deck),
      if (getDebugBoolean("ENABLE_MOBILITY")) mobilityEvents(world.definitions, world.deck, commands) else listOf()
  )
      .flatten() + spiritPursuitEvents + doomEvents(definitions, world)

  return eventsFromEvents(world, freedomTable, events)
}

fun gatherNextCommands(world: World, commands: Commands): Commands {
  val deck = world.deck
  val spirits = aliveSpirits(deck)
  val spiritResponseCommands = spiritsHandleRequests(world, spirits, commands)
  val interactCommands = gatherInteractCommands(deck, commands)
  return spiritResponseCommands + interactCommands
}
