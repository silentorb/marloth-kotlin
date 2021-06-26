package marloth.integration.misc

import marloth.clienting.ClientState
import marloth.clienting.PlayerBoxes
import marloth.clienting.editing.mainScene
import marloth.clienting.editing.renderEditorViewport
import marloth.clienting.flattenToPlayerBoxes
import marloth.clienting.gui.BloomDefinition
import marloth.clienting.gui.menus.logic.syncDisplayOptions
import marloth.clienting.gui.menus.logic.updateAppOptions
import marloth.clienting.gui.newBloomDefinition
import marloth.clienting.input.GuiCommandType
import marloth.clienting.updateClient
import marloth.integration.clienting.layoutGui
import marloth.integration.clienting.renderMain
import marloth.integration.clienting.updateAppStateForFirstNewPlayer
import marloth.integration.clienting.updateAppStateForNewPlayers
import marloth.integration.front.GameApp
import silentorb.mythic.bloom.Box
import silentorb.mythic.bloom.toAbsoluteBoundsRecursive
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.incrementGlobalDebugLoopNumber
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.ent.pipe
import silentorb.mythic.ent.singleValueCache
import silentorb.mythic.lookinglass.getPlayerViewports
import silentorb.mythic.quartz.updateTimestep
import silentorb.mythic.spatial.Vector2i
import silentorb.mythic.spatial.Vector4i
import simulation.entities.Player
import simulation.main.World
import simulation.updating.simulationDelta

fun updateClientPlayers(deckPlayers: Table<Player>): (List<Id>) -> List<Id> = { clientPlayers ->
  clientPlayers.plus(deckPlayers.keys.minus(clientPlayers))
}

fun updateClientFromWorld(worlds: List<World>, clientState: ClientState): ClientState {
  val world = worlds.lastOrNull()

  return if (world == null)
    clientState.copy(
        players = if (clientState.players.none())
          listOf(1L)
        else
          clientState.players
    )
  else
    clientState.copy(
        players = if (clientState.commands.any { it.type == GuiCommandType.newGame })
          listOf()
        else
          world.deck.players.keys.toList()
    )
}

fun getPlayerViewports(clientState: ClientState, windowDimensions: Vector2i): List<Vector4i> =
    getPlayerViewports(clientState.players.size, windowDimensions)

fun checkRestartGame(app: GameApp, appState: AppState, clientState: ClientState): AppState? {
  val newGameCommand = clientState.commands
      .firstOrNull { it.type == GuiCommandType.newGame }

  return if (newGameCommand != null) {
    val scene = newGameCommand.value as? String ?: mainScene()
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
          val deferredCommands = appState.worlds.lastOrNull()?.nextCommands ?: listOf()
          val clientState = updateClient(
              app.client,
              app.definitions.textLibrary,
              appState.options,
              appState.worlds,
              boxes,
              playerBloomDefinitions,
              appState.client,
              deferredCommands
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

  val windowInfo = app.client.getWindowInfo()
  if (steps <= 1 && windowInfo.dimensions.x > 0 && windowInfo.dimensions.y > 0) {
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
