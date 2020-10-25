package marloth.integration.misc

import marloth.clienting.ClientState
import marloth.clienting.input.newInputState
import marloth.clienting.rendering.marching.newMarchingState
import marloth.integration.front.GameApp
import silentorb.mythic.ent.Id
import silentorb.mythic.physics.releaseBulletState
import silentorb.mythic.quartz.newTimestepState
import simulation.main.World

fun restartWorld(app: GameApp, oldWorld: World): World {
  releaseBulletState(oldWorld.bulletState)
  return app.newWorld(app)
}

fun restartClientState(client: ClientState, playerMap: Map<Id, Id>): ClientState =
    ClientState(
        input = newInputState(client.input.config),
        guiStates = mapOf(),
        audio = client.audio,
        commands = listOf(),
        players = playerMap.values.toList(),
        marching = newMarchingState(),
        events = listOf(),
        displayModes = client.displayModes
    )

fun restartGame(app: GameApp, appState: AppState): AppState {
  System.gc()
  return if (appState.worlds.none()) {
    AppState(
        client = appState.client,
        options = appState.options,
        worlds = listOf(app.newWorld(app)),
        timestep = newTimestepState()
    )
  } else {
    val previousWorld = appState.worlds.last()
    val world = restartWorld(app, previousWorld)
    // Right now order doesn't matter for new player silentorb.mythic.characters since each one is identical other than location.
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
