package marloth.integration.misc

import marloth.clienting.ClientState
import marloth.clienting.editing.activeWorldKey
import marloth.clienting.editing.expandWorldGraph
import marloth.clienting.editing.loadWorldGraph
import marloth.clienting.editing.mainScene
import marloth.clienting.getNewGameCommand
import marloth.clienting.input.newInputState
import marloth.integration.front.GameApp
import marloth.integration.generation.generateNewWorld
import marloth.integration.generation.newGenerationConfig
import silentorb.mythic.debugging.getDebugBoolean
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.GraphLibrary
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.getMeshShapes
import silentorb.mythic.physics.releaseBulletState
import silentorb.mythic.platforming.PlatformInput
import silentorb.mythic.quartz.newTimestepState
import simulation.main.World
import kotlin.concurrent.thread

fun newWorld(gameApp: GameApp, graph: Graph, graphLibrary: GraphLibrary = mapOf()): World {
  val generationConfig = newGenerationConfig(gameApp, graphLibrary)
  return generateNewWorld(gameApp.db, graph, generationConfig)
}

fun restartWorld(app: GameApp, oldWorld: World, graph: Graph, graphLibrary: GraphLibrary): World {
  releaseBulletState(oldWorld.bulletState)
  return newWorld(app, graph, graphLibrary)
}

fun restartClientState(input: PlatformInput, client: ClientState, playerMap: Map<Id, Id>): ClientState =
    client.copy(
        input = newInputState(input),
        guiStates = mapOf(),
        commands = listOf(),
        players = playerMap.values.toList(),
        events = listOf(),
        isEditorActive = client.isEditorActive,
        isLoading = false,
    )

fun newOrRestartWorld(app: GameApp, appState: AppState, scene: String): World {
  val editor = appState.client.editor
  val graph = when {
    !getDebugBoolean("STATIC_MAP") -> listOf()
    editor != null -> expandWorldGraph(editor, scene)
    else -> loadWorldGraph(getMeshShapes(app.client.renderer), scene)
  }

  val graphLibrary = (editor?.graphLibrary ?: mapOf()).minus(activeWorldKey)
  return if (appState.worlds.none()) {
    newWorld(app, graph, graphLibrary)
  } else {
    val previousWorld = appState.worlds.last()
    restartWorld(app, previousWorld, graph, graphLibrary)
  }
}

fun newOrRestartGame(app: GameApp, appState: AppState, world: World): AppState {
  System.gc()

  return if (appState.worlds.none()) {
    val players = appState.client.players.associateWith { it }
    AppState(
        client = restartClientState(app.platform.input, appState.client, players),
        options = appState.options,
        worlds = listOf(world),
        timestep = newTimestepState()
    )
  } else {
    val previousWorld = appState.worlds.last()
    // Right now order doesn't matter for new player characters since each one is identical other than location.
    // Eventually they will need to be more carefully mapped to preserve proper association
    val players = previousWorld.deck.players.keys
        .zip(world.deck.players.keys)
        .associate { Pair(it.first, it.second) }

    AppState(
        client = restartClientState(app.platform.input, appState.client, players),
        options = appState.options,
        worlds = listOf(world),
        timestep = newTimestepState()
    )
  }
}

var asyncWorld: World? = null

fun checkRestartGame(app: GameApp, appState: AppState, clientState: ClientState): AppState? {
  val localAsyncAppState = asyncWorld
  return if (localAsyncAppState != null) {
    asyncWorld = null
    newOrRestartGame(app, appState.copy(client = clientState), localAsyncAppState)
  } else {
    val newGameCommand = getNewGameCommand(clientState.commands)

    if (newGameCommand != null) {
      val scene = newGameCommand.value as? String ?: mainScene()
      thread(start = true) {
        asyncWorld = newOrRestartWorld(app, appState.copy(client = clientState), scene)
        println("")
      }
      appState.copy(
          client = restartClientState(app.platform.input, appState.client, mapOf())
              .copy(
                  isLoading = true,
              )
      )
    } else
      null
  }
}
