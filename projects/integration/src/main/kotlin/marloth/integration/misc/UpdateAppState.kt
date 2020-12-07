package marloth.integration.misc

import marloth.clienting.*
import marloth.clienting.editing.defaultWorldScene
import marloth.clienting.editing.renderEditorViewport
import marloth.clienting.gui.hud.updateTargeting
import marloth.clienting.input.GuiCommandType
import marloth.clienting.input.mouseLookEvents
import marloth.clienting.gui.BloomDefinition
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.menus.logic.syncDisplayOptions
import marloth.clienting.gui.menus.logic.updateAppOptions
import marloth.clienting.gui.newBloomDefinition
import marloth.clienting.input.firstPlayer
import marloth.clienting.input.isGameMouseActive
import marloth.integration.clienting.layoutGui
import marloth.integration.clienting.renderMain
import marloth.integration.clienting.updateAppStateForFirstNewPlayer
import marloth.integration.clienting.updateAppStateForNewPlayers
import marloth.integration.front.GameApp
import marloth.scenery.enums.CharacterCommands
import persistence.Database
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.toAbsoluteBoundsRecursive
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.incrementGlobalDebugLoopNumber
import silentorb.mythic.ent.*
import silentorb.mythic.happenings.Command
import silentorb.mythic.happenings.Events
import silentorb.mythic.lookinglass.getPlayerViewports
import silentorb.mythic.quartz.updateTimestep
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4i
import simulation.entities.Player
import simulation.happenings.withSimulationEvents
import simulation.main.World
import simulation.updating.simulationDelta
import simulation.updating.updateWorld

fun updateSimulationDatabase(db: Database, next: World, previous: World) {
  val nextGameOver = next.global.gameOver
  if (previous.global.gameOver == null && nextGameOver != null) {
    if (nextGameOver.winningFaction == 1L) {
//      createVictory(db, Victory(
//          next.deck.players.values.first().name
//      ))
    }
  }
}

fun updateClientPlayers(deckPlayers: Table<Player>): (List<Id>) -> List<Id> = { clientPlayers ->
  clientPlayers.plus(deckPlayers.keys.minus(clientPlayers))
}

fun updateClientFromWorld(worlds: List<World>, clientState: ClientState): ClientState {
  val world = worlds.lastOrNull()

  return if (world == null)
    clientState
  else
    clientState.copy(
        players = if (clientState.commands.any { it.type == GuiCommandType.newGame })
          listOf()
        else
          updateClientPlayers(world.deck.players)(clientState.players)
    )
}

fun gatherAdditionalGameCommands(previousClient: ClientState, clientState: ClientState): List<Command> {
  return clientState.players.flatMap { player ->
    val guiState = clientState.guiStates[player]
    val view = guiState?.view
    val previousView = previousClient.guiStates[player]?.view
    listOfNotNull(
        if (previousView == ViewId.merchant &&
            clientState.commands.any { it.target == player && it.type == ClientEventType.menuBack })
//        if (view == null && previousView == ViewId.merchant)
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

fun getPlayerViewports(clientState: ClientState, windowDimensions: Vector2i): List<Vector4i> =
    getPlayerViewports(clientState.players.size, windowDimensions)

fun updateWorldGraph(events: Events, graph: Graph): Graph {
  val setGraphEvent = events.filterIsInstance<ClientEvent>().firstOrNull { it.type == ClientEventType.setWorldGraph }
  return if (setGraphEvent != null)
    setGraphEvent.value!! as Graph
  else
    graph
}

fun updateSimulation(app: GameApp, previousClient: ClientState, clientState: ClientState, worlds: List<World>, commands: List<Command>): List<World> {
  val world = worlds.last()
      .copy(
          staticGraph = updateWorldGraph(clientState.events, worlds.last().staticGraph),
      )

  val previous = worlds.takeLast(2).first()
  val gameCommands = filterCommands(clientState)(commands)
      .plus(gatherAdditionalGameCommands(previousClient, clientState))

  val definitions = app.definitions
  val windowResolution = app.client.renderer.options.windowedResolution
  val mouseEvents = if (isGameMouseActive(app.platform, clientState))
    listOf()
  else
    mouseLookEvents(windowResolution, previousClient.input.deviceStates.lastOrNull(), clientState.input.deviceStates.last(), firstPlayer(clientState))

  val clientEvents = clientState.events + gameCommands + mouseEvents
  val allEvents = withSimulationEvents(definitions, previous.deck, world, clientEvents)
  val nextWorld = updateWorld(definitions, allEvents, simulationDelta, world)
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

fun updateWorlds(app: GameApp, previousClient: ClientState, clientState: ClientState): (List<World>) -> List<World> = { worlds ->
  val commands = if (clientState.isEditorActive)
    listOf()
  else
    mapGameCommands(clientState.players, clientState.commands)

  updateSimulation(app, previousClient, clientState, worlds, commands)
}

fun checkRestartGame(app: GameApp, appState: AppState, clientState: ClientState): AppState? {
  val newGameCommand = clientState.commands
      .firstOrNull { it.type == GuiCommandType.newGame }

  return if (newGameCommand != null) {
    val scene = newGameCommand.value as? String ?: defaultWorldScene
    restartGame(app, appState.copy(client = clientState), scene)
  } else
    null
}

fun updateAppStateWorlds(app: GameApp, appState: AppState, clientState: ClientState): AppState {
  val worlds = if (appState.worlds.none() || getDebugBoolean("PAUSE_SIMULATION"))
    appState.worlds
  else
    updateWorlds(app, appState.client, clientState)(appState.worlds)

  return appState.copy(
      client = updateClientFromWorld(worlds, clientState),
      worlds = worlds,
      options = updateAppOptions(clientState, appState.options)
  )
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

          checkRestartGame(app, appState, clientState) ?: updateAppStateWorlds(app, appState, clientState)
        },
        updateAppStateForFirstNewPlayer,
        updateAppStateForNewPlayers
    )

fun layoutBoxes(app: GameApp, appState: AppState): Map<Id, Box> {
  val windowInfo = app.client.getWindowInfo()
  val viewports = getPlayerViewports(appState.client, windowInfo.dimensions)
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
      val onUpdate = appState.hooks?.onUpdate
      if (onUpdate != null) {
        onUpdate(nextState)
      }
      updateFixedIntervalSteps(app, layoutBoxes, remainingSteps - 1, nextState)
    }

fun updateAppState(app: GameApp): (AppState) -> AppState = { appState ->
  incrementGlobalDebugLoopNumber(60)
  val (timestep, steps) = updateTimestep(appState.timestep, simulationDelta.toDouble())
  val onTimeStep = appState.hooks?.onTimeStep
  if (onTimeStep != null) {
    onTimeStep(timestep, steps, appState)
  }

  val layoutBoxes = singleValueCache { source: AppState ->
    if (appState.client.isEditorActive)
      mapOf()
    else
      layoutBoxes(app, source)
  }

  var nextAppState = updateFixedIntervalSteps(app, layoutBoxes, steps, appState)
      .copy(
          timestep = timestep
      )

  if (steps <= 1) {
    val windowInfo = app.client.getWindowInfo()
    val viewports = getPlayerViewports(appState.client, windowInfo.dimensions)
    val boxes = layoutBoxes(nextAppState)
    val editor = nextAppState.client.editor
    if (appState.client.isEditorActive && editor != null) {
      val selectionQuery = renderEditorViewport(app.client, windowInfo, editor)
      if (selectionQuery != editor.selectionQuery) {
        nextAppState = nextAppState.copy(
            client = nextAppState.client.copy(
                editor = editor.copy(
                    selectionQuery = selectionQuery
                )
            )
        )
      }
    } else {
      renderMain(app.client, windowInfo, nextAppState, boxes, viewports)
    }
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
