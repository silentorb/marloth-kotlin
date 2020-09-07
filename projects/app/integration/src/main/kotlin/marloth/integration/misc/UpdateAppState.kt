package marloth.integration.misc

import marloth.clienting.ClientState
import marloth.clienting.MarlothBloomStateMap
import marloth.clienting.hud.updateTargeting
import marloth.clienting.input.GuiCommandType
import marloth.clienting.input.mouseLookEvents
import marloth.clienting.menus.BloomDefinition
import marloth.clienting.menus.ViewId
import marloth.clienting.menus.layoutPlayerGui
import marloth.clienting.menus.newBloomDefinition
import marloth.clienting.updateClient
import marloth.integration.clienting.renderMain
import marloth.integration.clienting.updateAppStateForFirstNewPlayer
import marloth.integration.clienting.updateAppStateForNewPlayers
import marloth.integration.front.GameApp
import marloth.integration.scenery.updateFlyThroughCamera
import marloth.scenery.enums.CharacterCommands
import persistence.Database
import persistence.createVictory
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.bloom.next.flattenBoxData
import silentorb.mythic.bloom.toAbsoluteBounds
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.debugging.incrementGlobalDebugLoopNumber
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.ent.pipe
import silentorb.mythic.happenings.CharacterCommand
import silentorb.mythic.happenings.Events
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

fun updateCurrentViews(world: World, playerViews: MarlothBloomStateMap): MarlothBloomStateMap {
  val deck = world.deck
//  val newEntries = deck.players.keys.mapNotNull { player ->
//    val interactingWith = getPlayerInteractingWith(deck, player)
//    val view = when {
//
//      world.global.gameOver != null -> ViewId.victory
//
//      interactingWith != null -> selectInteractionView(deck, interactingWith)
//
//      else -> null
//    }
//    if (view != null)
//      Pair(player, view)
//    else null
//  }
//      .associate { it }
//
//  return playerViews.plus(newEntries)
  return playerViews
}

fun updateClientPlayers(deckPlayers: Table<Player>): (List<Id>) -> List<Id> = { clientPlayers ->
  clientPlayers.plus(deckPlayers.keys.minus(clientPlayers))
}

fun updateClientFromWorld(worlds: List<World>): (ClientState) -> ClientState = { clientState ->
  val world = worlds.last()

  clientState.copy(
      players = if (clientState.commands.any { it.type == GuiCommandType.newGame })
        listOf()
      else
        updateClientPlayers(world.deck.players)(clientState.players),
      bloomStates = updateCurrentViews(world, clientState.bloomStates)
  )
}

fun gatherAdditionalGameCommands(previousClient: ClientState, clientState: ClientState): List<CharacterCommand> {
  return clientState.players.flatMap { player ->
    val view = clientState.bloomStates[player]?.view
    val previousView = previousClient.bloomStates[player]?.view
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
          val view = clientState.bloomStates[command.target]?.view
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
  val clientEvents = events + gameCommands + mouseLookEvents(app.client.renderer.config.dimensions, clientState.input.deviceStates.last(), previousClient.input.deviceStates.lastOrNull(), clientState.players.firstOrNull())
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
      val events = clientState.gameEvents
      updateSimulation(app, previousClient, clientState, worlds, commands, events)
    }
    else -> worlds.takeLast(1)
  }
}

fun updateFixedInterval(app: GameApp, boxes: Map<Id, Box>, playerBloomDefinitions: Map<Id, BloomDefinition>): (AppState) -> AppState =
    pipe(
        { appState ->
          app.platform.process.pollEvents()
          val clientState = updateClient(app.client, appState.worlds, boxes, playerBloomDefinitions, appState.client)
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
                worlds = worlds
            )
          }
        },
        updateAppStateForFirstNewPlayer,
        updateAppStateForNewPlayers
    )

fun layoutPlayerGui(app: GameApp, appState: AppState): (Id, Vector2i) -> Box = { player, dimensions ->
  val world = appState.worlds.lastOrNull()
  layoutPlayerGui(app.client.textResources, app.definitions, appState.client, world, dimensions, player)
}

fun layoutGui(app: GameApp, appState: AppState, dimensions: List<Vector2i>): Map<Id, Box> {
  val players = appState.client.players
  return if (players.none()) {
    mapOf()
  } else {
    players.zip(dimensions) { player, d -> player to layoutPlayerGui(app, appState)(player, d) }
        .associate { it }
  }
}

fun updateAppState(app: GameApp): (AppState) -> AppState = { appState ->
  incrementGlobalDebugLoopNumber(60)
  val windowInfo = app.client.getWindowInfo()
  val viewports = getPlayerViewports(appState.client.players.size, windowInfo.dimensions)
  val viewportDimensions = viewports.map { Vector2i(it.z, it.w) }
  val playerBoxes = layoutGui(app, appState, viewportDimensions)
  val playerBloomDefinitions = playerBoxes
      .mapValues {
        newBloomDefinition(flattenBoxData(listOf(it.value)))
      }
  val boxes = playerBoxes.mapValues { toAbsoluteBounds(Vector2i.zero, it.value) }
  val (timestep, steps) = updateTimestep(appState.timestep, simulationDelta.toDouble())
  val onTimeStep = app.hooks?.onTimeStep
  if (onTimeStep != null) {
    onTimeStep(timestep, steps, appState)
  }
  val nextMarching = if (steps <= 1)
    renderMain(app.client, windowInfo, appState, boxes.values, viewports)
  else
    appState.client.marching

  val nextAppState = appState.copy(
      client = appState.client.copy(
          marching = nextMarching
      )
  )

  (1..steps).fold(nextAppState) { state, step ->
    val newBoxes = if (step == 1)
      boxes
    else
      layoutGui(app, state, viewportDimensions)

    val result = updateFixedInterval(app, newBoxes, playerBloomDefinitions)(state)
    val onUpdate = app.hooks?.onUpdate
    if (onUpdate != null) {
      onUpdate(result)
    }
    result
  }
      .copy(
          timestep = timestep
      )
}
