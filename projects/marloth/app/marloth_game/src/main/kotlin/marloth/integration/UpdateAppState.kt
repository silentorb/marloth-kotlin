package marloth.integration

import haft.mapEventsToCommands
import marloth.clienting.*
import marloth.clienting.audio.updateAppStateAudio
import marloth.clienting.gui.ViewId
import marloth.clienting.gui.layoutGui
import marloth.clienting.input.GuiCommandType
import marloth.definition.templates.templates
import marloth.front.GameApp
import marloth.front.RenderHook
import mythic.bloom.next.Box
import mythic.bloom.toAbsoluteBounds
import mythic.ent.Id
import mythic.ent.pipe
import mythic.platforming.WindowInfo
import mythic.quartz.updateTimestep
import org.joml.Vector2i
import persistence.Database
import persistence.createVictory
import simulation.main.Deck
import simulation.main.World
import simulation.main.simulationDelta
import simulation.main.updateWorld
import simulation.misc.Command
import simulation.misc.CommandType
import simulation.misc.Victory
import simulation.misc.gameStrokes
import simulation.physics.newBulletState
import simulation.physics.releaseBulletState
import simulation.physics.syncNewBodies

fun updateSimulationDatabase(db: Database, next: World, previous: World) {
  val nextGameOver = next.gameOver
  if (previous.gameOver == null && nextGameOver != null) {
    if (nextGameOver.winningFaction == 1L)
      createVictory(db, Victory(
          next.players.first().name
      ))
  }
}

fun getPlayerInteractingWith(deck: Deck): Id? =
    deck.characters[deck.players.keys.first()]!!.interactingWith

fun updateClientFromWorld(worlds: List<World>, commands: List<Command>): (ClientState) -> ClientState =
    { client ->
      if (client.view != ViewId.none)
        client
      else {
        val world = worlds.last()
        val deck = world.deck
        val view = when {

          world.gameOver != null -> ViewId.victory

          getPlayerInteractingWith(deck) != null -> ViewId.merchant

          else -> null
        }

        if (view != null)
          syncBagWithCurrentView(client.copy(
              view = view
          ))
        else
          client
      }
    }

fun getGameCommands(state: AppState): List<Command> {
  val getBinding = getBinding(state.client.input, state.client.input.gameInputProfiles)
  return mapGameCommands(mapEventsToCommands(state.client.input.deviceStates, gameStrokes, getBinding))
}

fun updateAppWorld(app: GameApp, appState: AppState, commands: List<Command>): List<World> {
  val worlds = appState.worlds
  val world = worlds.last()
  val gameCommands = if (appState.client.view == ViewId.none)
    if (getPlayerInteractingWith(world.deck) != null)
      listOf(Command(type = CommandType.stopInteracting, target = 1))
    else
      commands
  else
    listOf()

  val animationDurations = app.client.renderer.animationDurations
  val nextWorld =
      updateWorld(app.bulletState, animationDurations, gameCommands, templates, simulationDelta)(world)
  updateSimulationDatabase(app.db, nextWorld, world)
  return worlds
      .plus(nextWorld)
      .takeLast(2)
}

fun restartWorld(app: GameApp, newWorld: () -> World): List<World> {
  releaseBulletState(app.bulletState)

  val world = newWorld()
  app.bulletState = newBulletState()
  syncNewBodies(world, app.bulletState)

  return listOf(world)
}

fun updateFixedInterval(app: GameApp, box: Box, newWorld: () -> World): (AppState) -> AppState = { appState ->
  app.platform.process.pollEvents()
  val nextClientState = pipe(appState.client, listOf(
      updateClientInput(app.client),
      updateClient(app.client, appState.players, box),
      updateAppStateAudio(app.client, appState.worlds)
  ))
  val newAppState = appState.copy(
      client = nextClientState
  )
  val commands = getGameCommands(newAppState)
  val worlds = when {
    nextClientState.commands.any { it.type == GuiCommandType.newGame } -> restartWorld(app, newWorld)
    gameIsActive(appState) -> updateAppWorld(app, newAppState, commands)
    else -> appState.worlds.takeLast(1)
  }

  appState.copy(
      client = updateClientFromWorld(worlds, commands)(nextClientState),
      worlds = worlds
  )
}

typealias GameUpdateHook = (AppState) -> Unit

data class GameHooks(
    val onRender: RenderHook,
    val onUpdate: GameUpdateHook
)

fun layoutGui(app: GameApp, appState: AppState, windowInfo: WindowInfo): Box {
  val world = appState.worlds.lastOrNull()
  val hudData = if (world != null)
    gatherHudData(world.deck, appState.client.view)
  else
    null

  return layoutGui(app.client, appState.client, world, hudData, windowInfo)
}

fun updateAppState(app: GameApp, newWorld: () -> World, hooks: GameHooks? = null): (AppState) -> AppState = { appState ->
  val windowInfo = app.client.getWindowInfo()
  val box = toAbsoluteBounds(Vector2i.zero, layoutGui(app, appState, windowInfo))
  val (timestep, steps) = updateTimestep(appState.timestep, simulationDelta.toDouble())

  if (steps <= 1) {
    renderMain(app.client, windowInfo, appState, box, hooks?.onRender)
  }

  (1..steps).fold(appState) { state, step ->
    val newBoxes = if (step == 1)
      box
    else
      layoutGui(app, state, windowInfo)

    val result = updateFixedInterval(app, newBoxes, newWorld)(state)
    if (hooks != null) {
      hooks.onUpdate(result)
    }
    result
  }
      .copy(
          timestep = timestep
      )
}
