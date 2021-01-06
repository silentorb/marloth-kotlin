package marloth.integration.misc

import marloth.clienting.ClientState
import marloth.clienting.editing.defaultWorldScene
import marloth.clienting.editing.expandDefaultWorldGraph
import marloth.clienting.editing.expandWorldGraph
import marloth.clienting.editing.loadWorldGraph
import marloth.clienting.input.newInputState
import marloth.integration.front.GameApp
import silentorb.mythic.editing.Editor
import silentorb.mythic.editing.getActiveEditorGraph
import silentorb.mythic.ent.Graph
import silentorb.mythic.ent.Id
import silentorb.mythic.lookinglass.getMeshShapes
import silentorb.mythic.physics.releaseBulletState
import silentorb.mythic.quartz.newTimestepState
import simulation.main.World

fun newWorld(gameApp: GameApp, graph: Graph): World {
  return generateWorld(gameApp.db, gameApp.definitions, getMeshShapes(gameApp.client.renderer), graph)
}

fun restartWorld(app: GameApp, oldWorld: World, graph: Graph): World {
  releaseBulletState(oldWorld.bulletState)
  return newWorld(app, graph)
}

fun restartClientState(client: ClientState, playerMap: Map<Id, Id>): ClientState =
    client.copy(
        input = newInputState(client.input.config),
        guiStates = mapOf(),
        commands = listOf(),
        players = playerMap.values.toList(),
        events = listOf(),
        isEditorActive = client.isEditorActive,
    )

fun restartGame(app: GameApp, appState: AppState, scene: String): AppState {
  System.gc()

  val editor = appState.client.editor
  val graph = if (editor != null)
    expandWorldGraph(editor, scene)
  else
    loadWorldGraph(getMeshShapes(app.client.renderer), scene)

  return if (appState.worlds.none()) {
    AppState(
        client = appState.client,
        options = appState.options,
        worlds = listOf(newWorld(app, graph)),
        timestep = newTimestepState()
    )
  } else {
    val previousWorld = appState.worlds.last()
    val world = restartWorld(app, previousWorld, graph)
    // Right now order doesn't matter for new player characters since each one is identical other than location.
    // Eventually they will need to be more carefully mapped to preserve proper association
    val players = previousWorld.deck.players.keys
        .zip(world.deck.players.keys)
        .associate { Pair(it.first, it.second) }
    AppState(
        client = restartClientState(appState.client, players),
        options = appState.options,
        worlds = listOf(world),
        timestep = newTimestepState()
    )
  }
}
