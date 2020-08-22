package marloth.integration.misc

import marloth.clienting.ClientState
import marloth.clienting.PlayerViews
import marloth.clienting.hud.updateTargeting
import marloth.clienting.menus.*
import marloth.clienting.input.GuiCommandType
import marloth.clienting.input.mouseLookEvents
import marloth.clienting.updateClient
import marloth.integration.clienting.renderMain
import marloth.integration.clienting.updateAppStateForFirstNewPlayer
import marloth.integration.clienting.updateAppStateForNewPlayers
import marloth.integration.front.GameApp
import silentorb.mythic.bloom.BloomState
import silentorb.mythic.bloom.next.Box
import silentorb.mythic.bloom.toAbsoluteBounds
import silentorb.mythic.ent.Id
import silentorb.mythic.ent.Table
import silentorb.mythic.debugging.incrementGlobalDebugLoopNumber
import silentorb.mythic.ent.pipe
import silentorb.mythic.quartz.updateTimestep
import silentorb.mythic.spatial.Vector2i
import persistence.Database
import persistence.createVictory
import silentorb.mythic.lookinglass.getPlayerViewports
import simulation.entities.Player
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.happenings.CharacterCommand
import marloth.scenery.enums.CharacterCommands
import silentorb.mythic.debugging.getDebugBoolean
import simulation.main.World
import simulation.updating.simulationDelta
import simulation.misc.Victory
import simulation.happenings.withSimulationEvents
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

fun updateCurrentViews(world: World, playerViews: PlayerViews): Map<Id, ViewId?> {
  val deck = world.deck
  val newEntries = deck.players.keys.mapNotNull { player ->
    val interactingWith = getPlayerInteractingWith(deck, player)
    val view = when {

      world.global.gameOver != null -> ViewId.victory

      interactingWith != null -> selectInteractionView(deck, interactingWith)

      else -> null
    }
    if (view != null)
      Pair(player, view)
    else null
  }
      .associate { it }

  return playerViews.plus(newEntries)
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
      playerViews = updateCurrentViews(world, clientState.playerViews)
  )
}

fun gatherAdditionalGameCommands(previousClient: ClientState, clientState: ClientState): List<CharacterCommand> {
  return clientState.players.flatMap { player ->
    val view = clientState.playerViews[player] ?: ViewId.none
    val previousView = previousClient.playerViews[player] ?: ViewId.none
    listOfNotNull(
        if (view == ViewId.none && previousView == ViewId.merchant)
          CharacterCommand(type = CharacterCommands.stopInteracting, target = player, device = 0)
        else
          null
    )
  }
}

fun guiEventsFromBloomState(bloomState: BloomState) = guiEvents(bloomState.bag)
    .filter { it.type == GuiEventType.gameEvent }
    .map { it.data as GameEvent }

fun guiEventsFromBloomStates(bloomStates: Map<Id, BloomState>) =
    bloomStates.values.flatMap(::guiEventsFromBloomState)

fun filterCommands(clientState: ClientState): (List<CharacterCommand>) -> List<CharacterCommand> = { commands ->
  commands
      .groupBy { it.target }
      .flatMap { (_, commands) ->
        commands.filter { command ->
          val view = clientState.playerViews[command.target] ?: ViewId.none
          view == ViewId.none
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
      val events = guiEventsFromBloomStates(clientState.bloomStates)
      updateSimulation(app, previousClient, clientState, worlds, commands, events)
    }
    else -> worlds.takeLast(1)
  }
}

fun updateFixedInterval(app: GameApp, boxes: List<Box>): (AppState) -> AppState =
    pipe(
        { appState ->
          app.platform.process.pollEvents()
          val clientState = updateClient(app.client, appState.worlds, boxes, appState.client)
          if (clientState.commands.any { it.type == GuiCommandType.newGame })
            restartGame(app, appState)
          else if (getDebugBoolean("PAUSE_SIMULATION") && appState.worlds.size > 1)
            appState
          else {
            val worlds = updateWorlds(app, appState.client, clientState)(appState.worlds)
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

fun layoutGui(app: GameApp, appState: AppState, dimensions: List<Vector2i>): List<Box> {
  val players = appState.client.players
  return if (players.none()) {
    listOf()
  } else {
    players.zip(dimensions, layoutPlayerGui(app, appState))
  }
}

fun updateAppState(app: GameApp): (AppState) -> AppState = { appState ->
  incrementGlobalDebugLoopNumber(60)
  val windowInfo = app.client.getWindowInfo()
  val viewports = getPlayerViewports(appState.client.players.size, windowInfo.dimensions)
  val viewportDimensions = viewports.map { Vector2i(it.z, it.w) }
  val nestedBoxes = layoutGui(app, appState, viewportDimensions)
  val boxes = nestedBoxes.map { toAbsoluteBounds(Vector2i.zero, it) }
  val (timestep, steps) = updateTimestep(appState.timestep, simulationDelta.toDouble())
  val onTimeStep = app.hooks?.onTimeStep
  if (onTimeStep != null) {
    onTimeStep(timestep, steps, appState)
  }
  val nextMarching = if (steps <= 1)
    renderMain(app.client, windowInfo, appState, boxes, viewports)
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

    val result = updateFixedInterval(app, newBoxes)(state)
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
