package marloth.integration.misc

import marloth.clienting.ClientState
import marloth.clienting.PlayerViews
import marloth.clienting.menus.*
import marloth.clienting.input.GuiCommandType
import marloth.clienting.updateClient
import marloth.integration.clienting.gatherHudData
import marloth.integration.clienting.renderMain
import marloth.integration.clienting.updateAppStateForFirstNewPlayer
import marloth.integration.clienting.updateAppStateForNewPlayers
import marloth.integration.front.GameApp
import marloth.integration.front.RenderHook
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
import silentorb.mythic.debugging.getDebugFloat
import silentorb.mythic.lookinglass.getPlayerViewports
import simulation.entities.Player
import silentorb.mythic.happenings.Events
import silentorb.mythic.happenings.GameEvent
import silentorb.mythic.happenings.CharacterCommand
import marloth.scenery.enums.CharacterCommands
import simulation.main.World
import simulation.updating.simulationDelta
import simulation.misc.Victory
import simulation.happenings.getSimulationEvents
import simulation.updating.updateWorld

fun updateSimulationDatabase(db: Database, next: World, previous: World) {
  val nextGameOver = next.gameOver
  if (previous.gameOver == null && nextGameOver != null) {
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

      world.gameOver != null -> ViewId.victory

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
          CharacterCommand(type = CharacterCommands.stopInteracting, target = player)
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
  val clientEvents = events.plus(gameCommands)
  val simulationEvents = getSimulationEvents(definitions, previous.deck, world, clientEvents)
  val allEvents = clientEvents + simulationEvents
  val nextWorld = updateWorld(definitions, allEvents, simulationDelta, world)
  updateSimulationDatabase(app.db, nextWorld, world)
  return worlds
      .plus(nextWorld)
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

typealias GameUpdateHook = (AppState) -> Unit

data class GameHooks(
    val onRender: RenderHook,
    val onUpdate: GameUpdateHook
)

fun layoutPlayerGui(app: GameApp, appState: AppState): (Id, Vector2i) -> Box = { player, dimensions ->
  val world = appState.worlds.lastOrNull()
  val hudData = if (world != null)
    gatherHudData(world, player, appState.client.playerViews[player]
        ?: ViewId.none)
  else
    null

  layoutPlayerGui(app.client.textResources, app.definitions, appState.client, world, hudData, dimensions, player)
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
  val nestedBoxes = layoutGui(app, appState, viewportDimensions)
  val boxes = nestedBoxes.map { toAbsoluteBounds(Vector2i.zero, it) }
  val (timestep, steps) = updateTimestep(appState.timestep, simulationDelta.toDouble())
  val minDroppedFrame = getDebugFloat("DROPPED_FRAME_MINIMUM")
  if (minDroppedFrame != null && timestep.rawDelta > minDroppedFrame) {
    println("Dropped frame: ${timestep.rawDelta}")
  }
  if (steps <= 1) {
    renderMain(app.client, windowInfo, appState, boxes, viewports)
  }

  (1..steps).fold(appState) { state, step ->
    val newBoxes = if (step == 1)
      boxes
    else
      layoutGui(app, state, viewportDimensions)

//    val box = Box(
//        boxes = boxes,
//        bounds = mergeBounds(newBoxes.map { it.bounds })
//    )
    val result = updateFixedInterval(app, newBoxes)(state)
    if (hooks != null) {
      hooks.onUpdate(result)
    }
    result
  }
      .copy(
          timestep = timestep
      )
}
