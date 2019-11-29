package marloth.integration

import haft.mapEventsToCommands
import marloth.clienting.ClientState
import marloth.clienting.gui.*
import marloth.clienting.input.GuiCommandType
import marloth.clienting.input.InputState
import marloth.clienting.input.getBinding
import marloth.clienting.updateClient
import marloth.front.GameApp
import marloth.front.RenderHook
import mythic.bloom.BloomState
import mythic.bloom.mergeBounds
import mythic.bloom.next.Box
import mythic.bloom.toAbsoluteBounds
import mythic.ent.Id
import mythic.ent.Table
import mythic.ent.incrementGlobalDebugLoopNumber
import mythic.ent.pipe
import mythic.quartz.updateTimestep
import org.joml.Vector2i
import persistence.Database
import persistence.createVictory
import rendering.getPlayerViewports
import simulation.entities.Player
import simulation.happenings.Events
import simulation.happenings.GameEvent
import simulation.input.Command
import simulation.input.CommandType
import simulation.input.gameStrokes
import simulation.main.World
import simulation.main.simulationDelta
import simulation.misc.Victory
import simulation.physics.newBulletState
import simulation.physics.releaseBulletState
import simulation.physics.syncNewBodies

fun updateSimulationDatabase(db: Database, next: World, previous: World) {
  val nextGameOver = next.gameOver
  if (previous.gameOver == null && nextGameOver != null) {
    if (nextGameOver.winningFaction == 1L)
      createVictory(db, Victory(
          next.deck.players.values.first().name
      ))
  }
}

fun updateCurrentViews(world: World, clientState: ClientState): Map<Id, ViewId?> {
  val deck = world.deck
  return deck.players.keys.mapNotNull { player ->
    val interactingWith = getPlayerInteractingWith(deck, player)
    val view = when {

      world.gameOver != null -> ViewId.victory

      interactingWith != null -> selectInteractionView(deck, interactingWith)

      else -> null
    }
    if (view != null)
      Pair(player, view)
    else null
  }
      .associate { it }
}

fun updateClientPlayers(deckPlayers: Table<Player>): (List<Id>) -> List<Id> = { clientPlayers ->
  clientPlayers.plus(deckPlayers.keys.minus(clientPlayers))
}

fun updateClientFromWorld(worlds: List<World>): (ClientState) -> ClientState = { clientState ->
  val world = worlds.last()
  clientState.copy(
      players = updateClientPlayers(world.deck.players)(clientState.players),
      playerViews = updateCurrentViews(world, clientState)
  )
}

fun gatherAdditionalGameCommands(previousClient: ClientState, clientState: ClientState): List<Command> {
  return clientState.players.flatMap { player ->
    val view = clientState.playerViews[player] ?: ViewId.none
    val previousView = previousClient.playerViews[player] ?: ViewId.none
    listOfNotNull(
        if (view == ViewId.none && previousView == ViewId.merchant)
          Command(type = CommandType.stopInteracting, target = player)
        else
          null
    )
  }
}

fun restartWorld(app: GameApp): List<World> {
  releaseBulletState(app.bulletState)
  val world = app.newWorld(app)
  app.bulletState = newBulletState()
  syncNewBodies(world, app.bulletState)
  return listOf(world)
}

fun gatherGuiEvents(bloomState: BloomState) = guiEvents(bloomState.bag)
    .filter { it.type == GuiEventType.gameEvent }
    .map { it.data as GameEvent }

fun filterCommands(clientState: ClientState): (List<Command>) -> List<Command> = { commands ->
  commands
      .groupBy { it.target }
      .flatMap { (_, commands) ->
        commands.filter { command ->
          val view = clientState.playerViews[command.target] ?: ViewId.none
          view == ViewId.none
        }
      }
}

fun updateSimulation(app: GameApp, previousClient: ClientState, clientState: ClientState, worlds: List<World>, commands: List<Command>, events: Events): List<World> {
  val world = worlds.last()
  val gameCommands = filterCommands(clientState)(commands)
      .plus(gatherAdditionalGameCommands(previousClient, clientState))

  val nextWorld = updateWorldFromClient(app, gameCommands, events)(world)
  updateSimulationDatabase(app.db, nextWorld, world)
  return worlds
      .plus(nextWorld)
      .takeLast(2)
}

fun updateWorlds(app: GameApp, previousClient: ClientState, clientState: ClientState): (List<World>) -> List<World> = { worlds ->
  when {
    clientState.commands.any { it.type == GuiCommandType.newGame } -> restartWorld(app)
    gameIsActiveByClient(clientState) -> {
      val commands = mapGameCommands(clientState.players, clientState.commands)
      val events = gatherGuiEvents(clientState.bloomState)
      updateSimulation(app, previousClient, clientState, worlds, commands, events)
    }
    else -> worlds.takeLast(1)
  }
}

fun updateFixedInterval(app: GameApp, box: Box): (AppState) -> AppState =
    pipe(
        { appState ->
          app.platform.process.pollEvents()
          val clientState = updateClient(app.client, appState.worlds, box)(appState.client)
          val worlds = updateWorlds(app, appState.client, clientState)(appState.worlds)

          appState.copy(
              client = updateClientFromWorld(worlds)(clientState),
              worlds = worlds
          )
        },
        updateAppStateForNewPlayers
    )

typealias GameUpdateHook = (AppState) -> Unit

data class GameHooks(
    val onRender: RenderHook,
    val onUpdate: GameUpdateHook
)

fun layoutPlayerGui(app: GameApp, appState: AppState): (Id, Vector2i) -> Box = { player, dimensions ->
  val world = appState.worlds.lastOrNull()
  val hudData = if (world != null)
    gatherHudData(world.deck, player, appState.client.playerViews[player] ?: ViewId.none)
  else
    null

  layoutPlayerGui(app.client, app.definitions, appState.client, world, hudData, dimensions, player)
}

fun layoutGui(app: GameApp, appState: AppState, dimensions: List<Vector2i>): List<Box> {
  val players = appState.client.players
  return if (players.none()) {
    listOf()
  } else {
    players.zip(dimensions, layoutPlayerGui(app, appState))
  }
}

fun updateAppState(app: GameApp, hooks: GameHooks? = null): (AppState) -> AppState = { appState ->
  incrementGlobalDebugLoopNumber(60)
  val windowInfo = app.client.getWindowInfo()
  val viewports = getPlayerViewports(appState.client.players.size, windowInfo.dimensions)
  val viewportDimensions = viewports.map { Vector2i(it.z, it.w) }
  val boxes = layoutGui(app, appState, viewportDimensions)
  val (timestep, steps) = updateTimestep(appState.timestep, simulationDelta.toDouble())

  if (steps <= 1) {
    renderMain(app.client, windowInfo, appState, boxes, viewports, hooks?.onRender)
  }

  (1..steps).fold(appState) { state, step ->
    val newBoxes = if (step == 1)
      boxes
    else
      layoutGui(app, state, viewportDimensions)

    val box = toAbsoluteBounds(Vector2i.zero, Box(
        boxes = boxes,
        bounds = mergeBounds(newBoxes.map { it.bounds })
    ))
    val result = updateFixedInterval(app, box)(state)
    if (hooks != null) {
      hooks.onUpdate(result)
    }
    result
  }
      .copy(
          timestep = timestep
      )
}
