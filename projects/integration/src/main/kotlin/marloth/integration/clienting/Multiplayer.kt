package marloth.integration.clienting

import marloth.clienting.input.defaultInputProfile
import marloth.clienting.input.joiningGamepads
import marloth.integration.misc.AppState
import silentorb.mythic.haft.DeviceIndexes
import simulation.characters.newPlayerAndCharacter
import simulation.main.addEntitiesToWorldDeck

val updateAppStateForNewPlayers: (AppState) -> AppState = { appState ->
  if (appState.worlds.none())
    appState
  else {
    val client = appState.client
    val inputState = client.input
    val deviceState = inputState.deviceStates.last()
    val deviceTypeMap = inputState.deviceTypeMap
    val devicePlayers = inputState.devicePlayers
    val world = appState.worlds.last()
    val deck = world.deck
    val graph = world.graph

    val gamepadJoinCommands = joiningGamepads(deviceState.events, deviceTypeMap)
    if (gamepadJoinCommands.none())
      appState
    else {
      val gamepadPlayers = devicePlayers.filter { deviceTypeMap[it.key]!! == DeviceIndexes.gamepad }.map { it.value }
      val availablePlayers = world.deck.players.keys.minus(gamepadPlayers)

      val nextWorld = addEntitiesToWorldDeck(world) { nextId ->
        gamepadJoinCommands.drop(availablePlayers.size)
            .flatMap { newPlayerAndCharacter(nextId, world.definitions, graph) }
      }
      val newPlayers = nextWorld.deck.players.keys.minus(deck.players.keys)
      val playersWithNewGamepads = newPlayers.plus(availablePlayers)
      appState.copy(
          client = client.copy(
              input = inputState.copy(
                  deviceTypeMap = deviceTypeMap + gamepadJoinCommands.associateWith { DeviceIndexes.gamepad },
                  devicePlayers = devicePlayers + gamepadJoinCommands.zip(playersWithNewGamepads).associate { Pair(it.first, it.second) },
                  playerProfiles = inputState.playerProfiles + playersWithNewGamepads.associateWith { defaultInputProfile }
              ),
              players = client.players.plus(newPlayers)
          ),
          worlds = appState.worlds.dropLast(1).plus(nextWorld)
      )
    }
  }
}
