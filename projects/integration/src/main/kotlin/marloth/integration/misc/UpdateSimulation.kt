package marloth.integration.misc

import marloth.clienting.ClientEvent
import marloth.clienting.ClientEventType
import marloth.clienting.ClientState
import marloth.clienting.editing.EditingMode
import marloth.clienting.gui.hud.updateTargeting
import marloth.clienting.input.firstPlayer
import marloth.clienting.input.isGameMouseActive
import marloth.clienting.input.mouseLookEvents
import marloth.integration.front.GameApp
import marloth.integration.generation.nextLevel
import marloth.scenery.enums.CharacterCommands
import persistence.Database
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.GraphWrapper
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import simulation.entities.remapPlayerRigCommands
import simulation.happenings.withSimulationEvents
import simulation.main.Deck
import simulation.main.World
import simulation.misc.Factions
import simulation.updating.updateWorld

fun updateSimulationDatabase(db: Database, next: World, previous: World) {
  val nextGameOver = next.global.gameOver
  if (previous.global.gameOver == null && nextGameOver != null) {
    if (nextGameOver.winningFaction == Factions.misfits) {
//      createVictory(db, Victory(
//          next.deck.players.values.first().name
//      ))
    }
  }
}

fun gatherAdditionalGameCommands(deck: Deck, previousClient: ClientState, clientState: ClientState): List<Command> {
  return clientState.players.flatMap { player ->
    val guiState = clientState.guiStates[player]
    val view = guiState?.view
    val previousView = previousClient.guiStates[player]?.view
    val character = deck.characters[player]
    listOfNotNull(
        if (character?.interactingWith != null && view == null && previousView != null)
          Command(type = CharacterCommands.stopInteracting, target = player)
        else
          null
    )
  }
}

fun filterCommands(clientState: ClientState): (List<Command>) -> List<Command> = { commands ->
  commands
      .groupBy { it.target }
      .flatMap { (_, commands) ->
        commands.filter { command ->
          val view = clientState.guiStates[command.target]?.view
          view == null
        }
      }
}

fun updateWorldGraph(events: Events, wrapper: GraphWrapper): GraphWrapper {
  val setGraphEvent = events.filterIsInstance<ClientEvent>().firstOrNull { it.type == ClientEventType.setWorldGraph }
  return if (setGraphEvent != null)
    GraphWrapper(setGraphEvent.value!! as Graph)
  else
    wrapper
}

fun updateWorldGraph(events: Events, world: World): World =
    world.copy(
        staticGraph = updateWorldGraph(events, world.staticGraph),
    )

fun gatherEventsForSimulation(app: GameApp, previousClient: ClientState, clientState: ClientState, world: World, previous: World, commands: List<Command>): Events {
  val definitions = app.definitions
  val dimensions = app.platform.display.getInfo().dimensions
  val gameCommands = filterCommands(clientState)(commands)
      .plus(gatherAdditionalGameCommands(world.deck, previousClient, clientState))

  val mouseEvents = if (isGameMouseActive(app.platform, clientState))
    listOf()
  else
    mouseLookEvents(dimensions, previousClient.input.deviceStates.lastOrNull(), clientState.input.deviceStates.last(), firstPlayer(clientState))

  val clientEvents = remapPlayerRigCommands(world.deck.players, clientState.events + gameCommands + mouseEvents)
  return withSimulationEvents(definitions, previous.deck, world, clientEvents + world.nextCommands)
}

fun updateSimulation(app: GameApp, previousClient: ClientState, clientState: ClientState, worlds: List<World>, commands: List<Command>): List<World> {
  val world = updateWorldGraph(clientState.events, worlds.last())

  return if (world.nextCommands.any { it.type == CharacterCommands.nextWorld }) {
    listOf(nextLevel(world))
  } else {
    val previous = worlds.takeLast(2).first()
    val allEvents = gatherEventsForSimulation(app, previousClient, clientState, world, previous, commands)
    val nextWorld = updateWorld(allEvents, 1, world)
    val finalWorld = nextWorld.copy(
        deck = nextWorld.deck.copy(
            targets = updateTargeting(nextWorld, app.client, clientState.players, commands, previousClient.commands, nextWorld.deck.targets)
        ),
    )

    updateSimulationDatabase(app.db, finalWorld, world)
    return worlds
        .plus(finalWorld)
        .takeLast(2)
  }
}

fun updateWorlds(app: GameApp, previousClient: ClientState, clientState: ClientState): (List<World>) -> List<World> = { worlds ->
  val commands = if (clientState.editingMode != EditingMode.none)
    listOf()
  else
    clientState.commands

  updateSimulation(app, previousClient, clientState, worlds, commands)
}
