package marloth.integration.misc

import marloth.clienting.*
import marloth.clienting.gui.hud.updateTargeting
import marloth.clienting.input.GuiCommandType
import marloth.clienting.input.mouseLookEvents
import marloth.clienting.gui.BloomDefinition
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.logic.syncDisplayOptions
import marloth.clienting.gui.menus.logic.updateAppOptions
import marloth.clienting.gui.newBloomDefinition
import marloth.integration.clienting.layoutGui
import marloth.integration.clienting.renderMain
import marloth.integration.clienting.updateAppStateForFirstNewPlayer
import marloth.integration.clienting.updateAppStateForNewPlayers
import marloth.integration.front.GameApp
import marloth.integration.scenery.updateFlyThroughCamera
import marloth.scenery.enums.CharacterCommands
import persistence.Database
import persistence.createVictory
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.toAbsoluteBoundsRecursive
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.incrementGlobalDebugLoopNumber
import silentorb.mythic.ent.*
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.lookinglass.getPlayerViewports
import silentorb.mythic.quartz.updateTimestep
import silentorb.mythic.spatial.Vector2i
import simulation.entities.Player
import simulation.happenings.withSimulationEvents
import simulation.main.World
import simulation.misc.Victory
import simulation.updating.simulationDelta
import simulation.updating.updateWorld

fun updateSimulationDatabase(db: Database, next: World, previous: World) {
  val nextGameOver = next.global.gameOver
  if (previous.global.gameOver == null && nextGameOver != null) {
    if (nextGameOver.winningFaction == 1L)
      createVictory(db, Victory(
          next.deck.players.values.first().name
      ))
  }
}

//fun updateCurrentViews(world: World, playerViews: MarlothBloomStateMap): MarlothBloomStateMap {
//  val deck = world.deck
////  val newEntries = deck.players.keys.mapNotNull { player ->
////    val interactingWith = getPlayerInteractingWith(deck, player)
////    val view = when {
////
////      world.global.gameOver != null -> ViewId.victory
////
////      interactingWith != null -> selectInteractionView(deck, interactingWith)
////
////      else -> null
////    }
////    if (view != null)
////      Pair(player, view)
////    else null
////  }
////      .associate { it }
////
////  return playerViews.plus(newEntries)
//  return playerViews
//}

fun updateClientPlayers(deckPlayers: Table<Player>): (List<Id>) -> List<Id> = { clientPlayers ->
  clientPlayers.plus(deckPlayers.keys.minus(clientPlayers))
}

fun updateClientFromWorld(worlds: List<World>): (ClientState) -> ClientState = { clientState ->
  val world = worlds.last()

  clientState.copy(
      players = if (clientState.commands.any { it.type == GuiCommandType.newGame })
        listOf()
      else
        updateClientPlayers(world.deck.players)(clientState.players)
  )
}

fun gatherAdditionalGameCommands(previousClient: ClientState, clientState: ClientState): List<CharacterCommand> {
  return clientState.players.flatMap { player ->
    val view = clientState.guiStates[player]?.view
    val previousView = previousClient.guiStates[player]?.view
    listOfNotNull(
        if (view == null && previousView == ViewId.merchant)
          CharacterCommand(type = CharacterCommands.stopInteracting, target = player, device = 0)
        else
          null
    )
  }
}

fun filterCommands(clientState: ClientState): (List<CharacterCommand>) -> List<CharacterCommand> = { commands ->
  commands
      .groupBy { it.target }
      .flatMap { (_, commands) ->
        commands.filter { command ->
          val view = clientState.guiStates[command.target]?.view
          view == null
        }
      }
}

fun updateSimulation(app: GameApp, previousClient: ClientState, clientState: ClientState, worlds: List<World>, commands: List<CharacterCommand>, events: Events): List<World> {
  val world = worlds.last()
  val previous = worlds.takeLast(2).first()
  val gameCommands = filterCommands(clientState)(commands)
      .plus(gatherAdditionalGameCommands(previousClient, clientState))

  val definitions = app.definitions
  val clientEvents = events + gameCommands + mouseLookEvents(app.client.renderer.options.windowedDimensions, clientState.input.deviceStates.last(), previousClient.input.deviceStates.lastOrNull(), clientState.players.firstOrNull())
  val allEvents = withSimulationEvents(definitions, previous.deck, world, clientEvents)
  val nextWorld = updateWorld(definitions, allEvents, simulationDelta, world)
  val finalWorld = nextWorld.copy(
      deck = nextWorld.deck.copy(
          targets = updateTargeting(nextWorld, app.client, clientState.players, commands, previousClient.commands, nextWorld.deck.targets)
      )
  )

  updateSimulationDatabase(app.db, finalWorld, world)
  return worlds
      .plus(finalWorld)
      .takeLast(2)
}

fun updateWorlds(app: GameApp, previousClient: ClientState, clientState: ClientState): (List<World>) -> List<World> = { worlds ->
  when {
    true -> {
      val commands = mapGameCommands(clientState.players, clientState.commands)
      val gameEvents = clientState.events.filterIsInstance<GameEvent>()
      updateSimulation(app, previousClient, clientState, worlds, commands, gameEvents)
    }
    else -> worlds.takeLast(1)
  }
}

fun updateFixedInterval(app: GameApp, boxes: PlayerBoxes, playerBloomDefinitions: Map<Id, BloomDefinition>): (AppState) -> AppState =
    pipe(
        { appState ->
          app.platform.process.pollEvents()
          val clientState = updateClient(
              app.client,
              appState.options,
              appState.worlds,
              boxes,
              playerBloomDefinitions,
              appState.client
          )
          if (getDebugBoolean("FLY_THROUGH_CAMERA")) {
            updateFlyThroughCamera(clientState)
          }
          if (clientState.commands.any { it.type == GuiCommandType.newGame })
            restartGame(app, appState)
          else {
            val worlds = if (getDebugBoolean("PAUSE_SIMULATION") && appState.worlds.size > 1)
              appState.worlds
            else
              updateWorlds(app, appState.client, clientState)(appState.worlds)

            appState.copy(
                client = updateClientFromWorld(worlds)(clientState),
                worlds = worlds,
                options = updateAppOptions(clientState, appState.options)
            )
          }
        },
        updateAppStateForFirstNewPlayer,
        updateAppStateForNewPlayers
    )

fun layoutBoxes(app: GameApp, appState: AppState): Map<Id, Box> {
  val windowInfo = app.client.getWindowInfo()
  val viewports = getPlayerViewports(appState.client.players.size, windowInfo.dimensions)
  val viewportDimensions = viewports.map { Vector2i(it.z, it.w) }
  val playerBoxes = layoutGui(app.definitions, appState, viewportDimensions)
  return playerBoxes.mapValues { toAbsoluteBoundsRecursive(it.value) }
}

fun updateFixedIntervalSteps(app: GameApp, layoutBoxes: (AppState) -> Map<Id, Box>, remainingSteps: Int, appState: AppState): AppState =
    if (remainingSteps == 0)
      appState
    else {
      val flatBoxes = flattenToPlayerBoxes(layoutBoxes(appState))
      val playerBloomDefinitions = flatBoxes
          .mapValues { newBloomDefinition(it.value) }
      val nextState = updateFixedInterval(app, flatBoxes, playerBloomDefinitions)(appState)
      val onUpdate = app.hooks?.onUpdate
      if (onUpdate != null) {
        onUpdate(nextState)
      }
      updateFixedIntervalSteps(app, layoutBoxes, remainingSteps - 1, nextState)
    }

fun updateAppState(app: GameApp): (AppState) -> AppState = { appState ->
  incrementGlobalDebugLoopNumber(60)
  val (timestep, steps) = updateTimestep(appState.timestep, simulationDelta.toDouble())
  val onTimeStep = app.hooks?.onTimeStep
  if (onTimeStep != null) {
    onTimeStep(timestep, steps, appState)
  }

  val layoutBoxes = singleValueCache { source: AppState -> layoutBoxes(app, source) }

  val nextAppState = updateFixedIntervalSteps(app, layoutBoxes, steps, appState)
      .copy(
          timestep = timestep
      )

  if (steps <= 1) {
    val windowInfo = app.client.getWindowInfo()
    val viewports = getPlayerViewports(appState.client.players.size, windowInfo.dimensions)
    val boxes = layoutBoxes(nextAppState)
    renderMain(app.client, windowInfo, nextAppState, boxes.values, viewports)
  }

  syncDisplayOptions(
      app.platform.display,
      appState.client,
      nextAppState.client,
      appState.options.display,
      nextAppState.options.display,
  )
  nextAppState
}
